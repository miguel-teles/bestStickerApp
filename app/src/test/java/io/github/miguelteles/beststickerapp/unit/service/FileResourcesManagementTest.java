package io.github.miguelteles.beststickerapp.unit.service;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.services.FileResourceManagement;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.utils.Utils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@RunWith(RobolectricTestRunner.class)
public class FileResourcesManagementTest {

    private ResourcesManagement resourcesManagement = null;
    private Uri uri;
    File resourcesFile = new File("src/test/resources");
    File doNotDelete = new File("src/test/resources/doNotDeleteText.txt");
    File doNotDeleteResult = new File("src/test/resources/doNotDeleteResult.txt");
    File doNotDeleteEmptyFile = new File("src/test/resources/doNotDeleteEmptyFile.txt");
    Uri tempFile;
    Uri tempFolder;

    @Before
    public void test() throws StickerFolderException {
        resourcesManagement = new FileResourceManagement(ApplicationProvider.getApplicationContext());
        uri = Uri.fromFile(resourcesFile);
        File tempFolder = new File(resourcesManagement.getCacheFolder().getPath(), "tempFolder");
        tempFolder.mkdir();
        this.tempFolder = Uri.fromFile(tempFolder);
        tempFile = resourcesManagement.getOrCreateFile(this.tempFolder, "temp.txt");
    }

    @Test
    public void testGetBaseDir() {
        Uri baseFolder = resourcesManagement.getBaseFolder();
        assertUriValues(baseFolder);
    }

    @Test
    public void testGetCacheFolder() {
        Uri cacheFolder = resourcesManagement.getCacheFolder();

        assertUriValues(cacheFolder);
    }

    @Test
    public void testGetOrCreateFile() throws StickerFolderException {
        Uri createdFile = resourcesManagement.getOrCreateFile(uri, "fileTest.jpg");
        assertUriValues(createdFile);

        createdFile = resourcesManagement.getOrCreateFile(uri, "fileTest.jpg");
        assertUriValues(createdFile);
    }

    @Test
    public void testGetOrCreateFileInvalidInput() throws StickerFolderException {
        assertThrows(IllegalArgumentException.class, () -> resourcesManagement.getOrCreateFile(null, "fileTest.jpg"));
        assertThrows(IllegalArgumentException.class, () -> resourcesManagement.getOrCreateFile(uri, null));
        assertThrows(IllegalArgumentException.class, () -> resourcesManagement.getOrCreateFile(uri, ""));
    }

    @Test
    public void testGetOrCreateStickerPackDirectory() {
        Uri pack = resourcesManagement.getOrCreateStickerPackDirectory("pack");
        assertUriValues(pack);
    }

    @Test
    public void testGetOrCreateStickerPackDirectoryInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> resourcesManagement.getOrCreateStickerPackDirectory(null));
        assertThrows(IllegalArgumentException.class, () -> resourcesManagement.getOrCreateStickerPackDirectory(""));
    }

    @Test
    public void testGetOrCreateLogsDirectory() {
        Uri logsDirectory = resourcesManagement.getOrCreateLogsDirectory();
        assertUriValues(logsDirectory);
    }

    @Test
    public void testGetOrCreateLogErrorsDirectory() {
        Uri errorsDirectory = resourcesManagement.getOrCreateLogErrorsDirectory();
        assertUriValues(errorsDirectory);
    }

    @Test
    public void testGetFilesFromDirectory() throws StickerFolderException {
        Uri resourcesFolder = Uri.fromFile(new File("src/test/resources"));

        List<Uri> filesFromDirectory = resourcesManagement.getFilesFromDirectory(resourcesFolder);
        assertNotNull(filesFromDirectory);
        assertFalse(filesFromDirectory.isEmpty());
        for (Uri uri : filesFromDirectory) {
            assertUriValues(uri);
        }
    }

    @Test
    public void testGetFilesFromDirectoryInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> resourcesManagement.getFilesFromDirectory(null));
    }

    @Test
    public void testWriteToFile() throws StickerFolderException, IOException {
        resourcesManagement.writeToFile(Uri.fromFile(doNotDeleteResult), ApplicationProvider.getApplicationContext().getContentResolver().openInputStream(Uri.fromFile(doNotDeleteEmptyFile)));
        assertTrue(Files.readLines(doNotDeleteResult, StandardCharsets.UTF_8).isEmpty());

        resourcesManagement.writeToFile(Uri.fromFile(doNotDeleteResult), ApplicationProvider.getApplicationContext().getContentResolver().openInputStream(Uri.fromFile(doNotDelete)));
        assertNotNull(Files.readLines(doNotDeleteResult, StandardCharsets.UTF_8));
    }

    @Test
    public void testCopyImageToStickerPackFolderInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> resourcesManagement.writeToFile(null, ApplicationProvider.getApplicationContext().getContentResolver().openInputStream(Uri.fromFile(doNotDelete))));
        assertThrows(IllegalArgumentException.class, () -> resourcesManagement.writeToFile(Uri.fromFile(doNotDeleteResult), null));
    }

    @Test
    public void testDeleteFile() throws StickerFolderException {
        File file = new File(tempFolder.getPath());
        assertTrue(file.exists());
        resourcesManagement.deleteFile(tempFolder);

        assertFalse(file.exists());
    }

    @Test
    public void testDeleteFileInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> resourcesManagement.deleteFile(null));
    }

    @Test
    public void testGetFileExtension() throws StickerFolderException {
        String content = resourcesManagement.getContentAsString(Uri.fromFile(doNotDeleteResult));
        assertFalse(Utils.isNothing(content));
    }

    @Test
    public void testGetFileExtensionInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> resourcesManagement.getContentAsString(null));
    }

    private void assertUriValues(Uri pack) {
        assertNotNull(pack);
        assertNotNull(pack.getPath());
        assertEquals(ContentResolver.SCHEME_FILE, pack.getScheme());
    }

}
