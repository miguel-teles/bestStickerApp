package io.github.miguelteles.beststickerapp.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;

import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFolderExceptionEnum;
import io.github.miguelteles.beststickerapp.services.interfaces.FoldersManagementService;

public class StickerImageConvertionService {

    private static StickerImageConvertionService instance;

    private final FoldersManagementService foldersManagementService;

    private StickerImageConvertionService() {
        foldersManagementService = FoldersManagementServiceImpl.getInstance();
    }

    public static StickerImageConvertionService getInstance() {
        if (instance == null) {
            instance = new StickerImageConvertionService();
        }
        return instance;
    }

    public String getFileExtension(File file, boolean withDot) {
        String fileName = file.getName();
        String result = fileName.substring(fileName.lastIndexOf("."));
        if (!withDot) {
            result = result.replace(".", "");
        }
        return result;
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
        try {
            File sourceImage = new File(selectedImageSourceAbsolutePath);
            int rotation = getImageOrientation(selectedImageSourceAbsolutePath);

            String stickerPackImageFileName = destinationImageFileName + getFileExtension(sourceImage, true);
            String stickerPackImageResizedFileName = destinationImageFileName + "Rzd.webp"; //TEM QUE SER .webp se não o whatsapp não aceita

            File stickerPackOriginalImageAbsoluteFile = null;
            if (keepOriginalCopy) {
                stickerPackOriginalImageAbsoluteFile = new File(stickerPackFolder, stickerPackImageFileName);
                copyImageFromSourceToDestination(sourceImage, stickerPackOriginalImageAbsoluteFile);
                resizeAndRotateImage(stickerPackOriginalImageAbsoluteFile, determineImageSmallerSide(stickerPackOriginalImageAbsoluteFile), FoldersManagementServiceImpl.TRAY_IMAGE_MAX_FILE_SIZE, rotation);
            }
            File stickerPackResizedImageAbsoluteFile = new File(stickerPackFolder, stickerPackImageResizedFileName);
            copyImageFromSourceToDestination(sourceImage, stickerPackResizedImageAbsoluteFile);
            resizeAndRotateImage(stickerPackResizedImageAbsoluteFile, imageWidthAndHeight, FoldersManagementServiceImpl.TRAY_IMAGE_MAX_FILE_SIZE, rotation);

            byte[] bytes = null;
            try (InputStream inputStream = Files.newInputStream(stickerPackResizedImageAbsoluteFile.toPath())) {
                bytes = foldersManagementService.readBytesFromInputStream(inputStream, stickerPackResizedImageAbsoluteFile.getName());
            }
            return new FoldersManagementService.Image(stickerPackOriginalImageAbsoluteFile, stickerPackResizedImageAbsoluteFile, bytes);
        } catch (StickerFolderException ste) {
            throw ste;
        } catch (Exception ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.COPY, "Erro ao copiar foto do pacote para a pasta do pacote " + stickerPackFolder.getName());
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


    private int determineImageSmallerSide(File stickerPackImageFile) {
        Bitmap bitmap = BitmapFactory.decodeFile(stickerPackImageFile.getAbsolutePath());
        int greaterSide = bitmap.getWidth();
        if (greaterSide < bitmap.getHeight()) {
            greaterSide = bitmap.getHeight();
        }
        return greaterSide;
    }

    private void copyImageFromSourceToDestination(File sourceFile, File destinationFile) throws StickerFolderException {
        try {
            Files.copy(sourceFile.toPath(), destinationFile.toPath());
        } catch (Exception ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.COPY, "Erro ao copiar file");
        }
    }

    private void resizeAndRotateImage(File imageToResize,
                                      int imageWidthAndHeight,
                                      int fileSize,
                                      int rotation) throws StickerFolderException {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imageToResize.getAbsolutePath());

            bitmap = applyRotationToBitmap(bitmap, rotation);
            bitmap = Bitmap.createScaledBitmap(bitmap, imageWidthAndHeight, imageWidthAndHeight, false);

            FileOutputStream out = new FileOutputStream(imageToResize);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.RESIZE, "Imagem: " + imageToResize.getName());
        }
    }

    public File convertImage(File file) {

        try (InputStream inputStream = Files.newInputStream(file.toPath())) {

            byte[] encodedImageInBase64 = foldersManagementService.readBytesFromInputStream(inputStream, file.getName());
            String = Base64.getEncoder().encodeToString(encodedImageInBase64);

        }

    }

}
