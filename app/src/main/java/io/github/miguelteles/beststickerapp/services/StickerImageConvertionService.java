package io.github.miguelteles.beststickerapp.services;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

import android.net.Uri;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIConvertedWebp;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFatalErrorException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFolderExceptionEnum;
import io.github.miguelteles.beststickerapp.services.client.ImageConverterWebpAPIImpl;
import io.github.miguelteles.beststickerapp.services.client.interfaces.ImageConverterWebpAPI;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.validator.MethodInputValidator;

public class StickerImageConvertionService {

    private static StickerImageConvertionService instance;
    private final ResourcesManagement resourcesManagement;
    private final ImageConverterWebpAPI imageConverterWebpAPI;
    private final ContentResolver contentResolver;

    private StickerImageConvertionService() throws StickerException {
        resourcesManagement = FileResourceManagement.getInstance();
        imageConverterWebpAPI = new ImageConverterWebpAPIImpl(Utils.getApplicationContext());
        contentResolver = Utils.getApplicationContext().getContentResolver();
    }

    public StickerImageConvertionService(ResourcesManagement resourcesManagement,
                                         ImageConverterWebpAPI imageConverterWebpAPI,
                                         ContentResolver contentResolver) {
        this.resourcesManagement = resourcesManagement;
        this.imageConverterWebpAPI = imageConverterWebpAPI;
        this.contentResolver = contentResolver;
    }

    public static StickerImageConvertionService getInstance() throws StickerException {
        if (instance == null) {
            instance = new StickerImageConvertionService();
        }
        return instance;
    }

    public ResourcesManagement.Image generateStickerImages(@NotNull Uri stickerPackFolder,
                                                           @NotNull Uri sourceImage,
                                                           @NotNull String destinationImageFileName,
                                                           @NotNull Integer imageWidthAndHeight,
                                                           boolean keepOriginalCopy) throws StickerException {
        MethodInputValidator.requireNotNull(stickerPackFolder, "stickerPackFolder");
        MethodInputValidator.requireNotNull(sourceImage, "sourceImage");
        MethodInputValidator.requireNotEmpty(destinationImageFileName, "destinationImageFileName");
        MethodInputValidator.requireNotEmpty(imageWidthAndHeight, "imageWidthAndHeight");

        Uri originalImageCopy = null;
        Uri resizedImageOriginalFormat = null;
        try {
            int rotation = getImageOrientation(sourceImage);

            String stickerPackImageFileName = destinationImageFileName + this.resourcesManagement.getFileExtension(sourceImage, true);
            String stickerPackImageResizedFileName = destinationImageFileName + this.resourcesManagement.getFileExtension(sourceImage, true); //TEM QUE SER .webp se não o whatsapp não aceita

            if (keepOriginalCopy) {
                originalImageCopy = generateImageCopy(sourceImage, stickerPackFolder, stickerPackImageFileName);
                rotateImage(originalImageCopy, rotation);
            }
            resizedImageOriginalFormat = generateImageCopy(sourceImage, resourcesManagement.getCacheFolder(), stickerPackImageResizedFileName);
            rotateImage(resizedImageOriginalFormat, rotation);
            resizeImage(resizedImageOriginalFormat, imageWidthAndHeight);

            Uri resizedImageWebp = convertImageToWebp(resizedImageOriginalFormat, stickerPackFolder);
            byte[] bytes = readBytesFromGeneratedImageWebp(resizedImageWebp);
            return new ResourcesManagement.Image(originalImageCopy, resizedImageWebp, bytes);
        } catch (StickerException ste) {
            throw ste;
        } catch (Exception ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.COPY, "Erro ao copiar foto do pacote para a pasta do pacote " + stickerPackFolder.getLastPathSegment());
        } finally {
            if (resizedImageOriginalFormat != null) {
                resourcesManagement.deleteFile(resizedImageOriginalFormat);
            }
        }
    }

    private byte[] readBytesFromGeneratedImageWebp(Uri resizedImageWebp) throws IOException, StickerFolderException {
        byte[] bytes = null;
        try (InputStream inputStream = contentResolver.openInputStream(resizedImageWebp)) {
            bytes = resourcesManagement.readBytesFromInputStream(inputStream);
        }
        return bytes;
    }

    private Uri generateImageCopy(Uri sourceImage,
                                  Uri copyDestinationFolder,
                                  String copyDestinationFileName) throws Exception {
        Uri resizedImageOriginalFormat = resourcesManagement.getOrCreateFile(copyDestinationFolder, copyDestinationFileName);
        resourcesManagement.writeToFile(resizedImageOriginalFormat, contentResolver.openInputStream(sourceImage));
        return resizedImageOriginalFormat;
    }

    private Bitmap applyRotationToBitmap(Bitmap bitmap, int rotate) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * Por algum motivo, as imagens tiradas das câmeras da Samsung são viradas em 90 graus. Então quando copiamos a imagem
     **/
    private int getImageOrientation(Uri imagePath) {
        int rotate = 0;
        try (InputStream imageInputStream = contentResolver.openInputStream(imagePath)) {
            if (imageInputStream == null) {
                throw new NullPointerException("Não foi possível abrir inputStream da imagem a ser rotacionada");
            }
            ExifInterface exif = new ExifInterface(imageInputStream);
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

    private void rotateImage(Uri imageToRotate,
                             int rotation) throws StickerFolderException {
        Bitmap bitmap = BitmapFactory.decodeFile(imageToRotate.getPath());
        try (FileOutputStream out = new FileOutputStream(imageToRotate.getPath())) {
            bitmap = applyRotationToBitmap(bitmap, rotation);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
        } catch (Exception ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.RESIZE, "Rotacionando imagem: " + imageToRotate.getLastPathSegment());
        }
    }

    private void resizeImage(Uri imageToResize,
                             int imageWidthAndHeight) throws StickerFolderException {
        Bitmap bitmap = BitmapFactory.decodeFile(imageToResize.getPath());
        try (FileOutputStream out = new FileOutputStream(imageToResize.getPath())) {
            bitmap = Bitmap.createScaledBitmap(bitmap, imageWidthAndHeight, imageWidthAndHeight, false);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
        } catch (Exception ex) {
            throw new StickerFolderException(ex, StickerFolderExceptionEnum.RESIZE, "Imagem: " + imageToResize.getLastPathSegment());
        }
    }

    private Uri convertImageToWebp(Uri file, Uri destinationFolder) throws StickerException {
        String originalFormatImageBase64 = convertFileIntoBase64(file);
        ResponseAPIConvertedWebp responseAPIConvertedWebp = this.imageConverterWebpAPI.convertImageToWebp(originalFormatImageBase64);
        if (responseAPIConvertedWebp.getMessage() != null || responseAPIConvertedWebp.getWebpImageBase64() == null) {
            throw new StickerFolderException(null, StickerFolderExceptionEnum.CONVERT_FILE, responseAPIConvertedWebp.getMessage());
        }

        byte[] webpImageInByteArray = Base64.getDecoder().decode(responseAPIConvertedWebp.getWebpImageBase64());
        Uri webpImage = this.resourcesManagement.getOrCreateFile(destinationFolder, file.getLastPathSegment().replace(this.resourcesManagement.getFileExtension(file, true), ".webp"));
        this.resourcesManagement.writeToFile(webpImage, new ByteArrayInputStream(webpImageInByteArray));
        return webpImage;
    }

    private String convertFileIntoBase64(Uri uri) throws StickerFolderException {
        String originalFormatImageBase64;
        try (InputStream inputStream = contentResolver.openInputStream(uri)) {
            byte[] encodedImageInBase64 = resourcesManagement.readBytesFromInputStream(inputStream);
            originalFormatImageBase64 = Base64.getEncoder().encodeToString(encodedImageInBase64);
        } catch (IOException e) {
            throw new StickerFolderException(e, StickerFolderExceptionEnum.GET_FILE, "Erro ao ler imagem e converter para base64");
        }
        return originalFormatImageBase64;
    }

}
