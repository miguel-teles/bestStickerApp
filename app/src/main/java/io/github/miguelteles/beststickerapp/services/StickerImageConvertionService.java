package io.github.miguelteles.beststickerapp.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;

import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIConvertedWebpDTO;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFolderExceptionEnum;
import io.github.miguelteles.beststickerapp.services.client.ImageConverterWebpAPI;
import io.github.miguelteles.beststickerapp.utils.Utils;

public class StickerImageConvertionService {

    private static StickerImageConvertionService instance;
    private final FoldersManagementService foldersManagementService;
    private final ImageConverterWebpAPI imageConverterWebpAPI;

    private StickerImageConvertionService() {
        foldersManagementService = FoldersManagementService.getInstance();
        imageConverterWebpAPI = new ImageConverterWebpAPI();
    }

    public static StickerImageConvertionService getInstance() {
        if (instance == null) {
            instance = new StickerImageConvertionService();
        }
        return instance;
    }

    public FoldersManagementService.Image generateStickerImages(File stickerPackFolder,
                                                                Uri selectedImageSourceUri,
                                                                String destinationImageFileName,
                                                                Integer imageWidthAndHeight,
                                                                boolean keepOriginalCopy) throws StickerFolderException {
        return this.generateStickerImages(stickerPackFolder,
                foldersManagementService.getAbsolutePathFromURI(selectedImageSourceUri),
                destinationImageFileName,
                imageWidthAndHeight,
                keepOriginalCopy);
    }

    public FoldersManagementService.Image generateStickerImages(File stickerPackFolder,
                                                                String selectedImageSourceAbsolutePath,
                                                                String destinationImageFileName,
                                                                Integer imageWidthAndHeight,
                                                                boolean keepOriginalCopy) throws StickerFolderException {
        File originalImage = null;
        File resizedImageOriginalFormat = null;
        try {
            File sourceImage = new File(selectedImageSourceAbsolutePath);
            int rotation = getImageOrientation(selectedImageSourceAbsolutePath);

            String stickerPackImageFileName = destinationImageFileName + this.foldersManagementService.getFileExtension(sourceImage, true);
            String stickerPackImageResizedFileName = destinationImageFileName + this.foldersManagementService.getFileExtension(sourceImage, true); //TEM QUE SER .webp se não o whatsapp não aceita

            if (keepOriginalCopy) {
                originalImage = new File(stickerPackFolder, stickerPackImageFileName);
                copyImageFromSourceToDestination(sourceImage, originalImage);
                rotateImage(originalImage, rotation);
            }
            resizedImageOriginalFormat = new File(Utils.getApplicationContext().getCacheDir(), stickerPackImageResizedFileName);
            copyImageFromSourceToDestination(sourceImage, resizedImageOriginalFormat);
            resizeImage(resizedImageOriginalFormat, imageWidthAndHeight);
            rotateImage(resizedImageOriginalFormat, rotation);
            File resizedImageWebp = convertImageToWebp(resizedImageOriginalFormat, stickerPackFolder);

            byte[] bytes = null;
            try (InputStream inputStream = Files.newInputStream(resizedImageWebp.toPath())) {
                bytes = foldersManagementService.readBytesFromInputStream(inputStream, resizedImageOriginalFormat.getName());
            }
            return new FoldersManagementService.Image(originalImage, resizedImageWebp, bytes);
        } catch (StickerFolderException ste) {
            throw ste;
        } catch (Exception ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.COPY, "Erro ao copiar foto do pacote para a pasta do pacote " + stickerPackFolder.getName());
        } finally {
            foldersManagementService.deleteFile(resizedImageOriginalFormat);
        }
    }

    private Bitmap applyRotationToBitmap(Bitmap bitmap, int rotate) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * Por algum motivo, as imagens tiradas das câmeras da Samsung são viradas em 90 graus. Então quando copiamos a imagem
     **/
    private int getImageOrientation(String imagePath) {
        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate;
    }

    private void copyImageFromSourceToDestination(File sourceFile, File destinationFile) throws StickerFolderException {
        try {
            Files.copy(sourceFile.toPath(), destinationFile.toPath());
        } catch (Exception ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.COPY, "Erro ao copiar file");
        }
    }

    private void rotateImage(File imageToRotate,
                             int rotation) throws StickerFolderException {
        Bitmap bitmap = BitmapFactory.decodeFile(imageToRotate.getAbsolutePath());
        try (FileOutputStream out = new FileOutputStream(imageToRotate)){
            bitmap = applyRotationToBitmap(bitmap, rotation);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
        } catch (Exception ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.RESIZE, "Rotacionando imagem: " + imageToRotate.getName());
        }
    }

    private void resizeImage(File imageToResize,
                             int imageWidthAndHeight) throws StickerFolderException {
        Bitmap bitmap = BitmapFactory.decodeFile(imageToResize.getAbsolutePath());
        try (FileOutputStream out = new FileOutputStream(imageToResize)) {
            bitmap = Bitmap.createScaledBitmap(bitmap, imageWidthAndHeight, imageWidthAndHeight, false);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
        } catch (Exception ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.RESIZE, "Imagem: " + imageToResize.getName());
        }
    }

    public File convertImageToWebp(File file, File destinationFolder) throws StickerException {
        String originalFormatImageBase64 = convertFileIntoBase64(file);
        ResponseAPIConvertedWebpDTO responseAPIConvertedWebpDTO = this.imageConverterWebpAPI.convertImageToWebp(originalFormatImageBase64);
        if (responseAPIConvertedWebpDTO.getMessage() != null) {
            throw new StickerFolderException(null, StickerFolderExceptionEnum.CONVERT_FILE, responseAPIConvertedWebpDTO.getMessage());
        }

        byte[] webpImageInByteArray = Base64.getDecoder().decode(responseAPIConvertedWebpDTO.getWebpImageBase64());
        File result = new File(destinationFolder, file.getName().replace(this.foldersManagementService.getFileExtension(file, true), ".webp"));
        try (FileOutputStream out = new FileOutputStream(result)){
            out.write(webpImageInByteArray);
            return result;
        } catch (IOException e) {
            throw new StickerFolderException(e, StickerFolderExceptionEnum.CONVERT_FILE, "Erro ao converter imagem para .webp");
        }
    }

    private String convertFileIntoBase64(File file) throws StickerFolderException {
        String originalFormatImageBase64;
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            byte[] encodedImageInBase64 = foldersManagementService.readBytesFromInputStream(inputStream, file.getName());
            originalFormatImageBase64 = Base64.getEncoder().encodeToString(encodedImageInBase64);
        } catch (IOException e) {
            throw new StickerFolderException(e, StickerFolderExceptionEnum.GET_FILE, "Erro ao ler imagem e converter para base64");
        }
        return originalFormatImageBase64;
    }

}
