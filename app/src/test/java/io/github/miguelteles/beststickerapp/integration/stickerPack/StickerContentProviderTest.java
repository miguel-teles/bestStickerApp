package io.github.miguelteles.beststickerapp.integration.stickerPack;

import static org.junit.Assert.*;

import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.ANIMATED_STICKER_PACK;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.AVOID_CACHE;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.FOLDER;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.IMAGE_DATA_VERSION;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.IOS_APP_DOWNLOAD_LINK_IN_QUERY;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.LICENSE_AGREEMENT_WEBSITE;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.PRIVACY_POLICY_WEBSITE;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.PUBLISHER_EMAIL;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.PUBLISHER_WEBSITE;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKER_FILE_EMOJI_IN_QUERY;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKER_FILE_NAME_IN_QUERY;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKER_IDENTIFIER;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKER_PACK_ICON_IN_QUERY;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKER_PACK_ICON_ORIGINAL_IMAGE_FILE;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKER_PACK_IDENTIFIER;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKER_PACK_NAME_IN_QUERY;
import static io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider.STICKER_PACK_PUBLISHER_IN_QUERY;

import android.content.ContentProvider;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerContentProvider;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerUriProvider;
import io.github.miguelteles.beststickerapp.services.FileResourceManagement;
import io.github.miguelteles.beststickerapp.services.StickerImageConvertionService;
import io.github.miguelteles.beststickerapp.services.StickerPackService;
import io.github.miguelteles.beststickerapp.services.StickerService;
import io.github.miguelteles.beststickerapp.services.client.ImageConverterWebpAPIImpl;
import io.github.miguelteles.beststickerapp.services.interfaces.OperationCallback;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.validator.StickerPackValidator;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.LOLLIPOP) // Lollipop is API 21
public class StickerContentProviderTest {

    private ResourcesManagement resourcesManagement;
    private StickerService stickerService;
    private StickerContentProviderReader stickerContentProviderReader;
    private StickerPackService stickerPackService;
    private Uri stickerPackImage = Uri.fromFile(new File("src/test/resources/io/github/miguelteles/beststickerapp/unit/service/test_image.jpg"));

    private StickerPack createdStickerPack;

    @Before
    public void init() throws StickerException {
        resourcesManagement = new FileResourceManagement(ApplicationProvider.getApplicationContext(),
                ApplicationProvider.getApplicationContext().getContentResolver());

        StickerImageConvertionService stickerImageConvertionService = new StickerImageConvertionService(resourcesManagement,
                new ImageConverterWebpAPIImpl(ApplicationProvider.getApplicationContext()),
                ApplicationProvider.getApplicationContext().getContentResolver());

        stickerService = new StickerService(new StickerMockRepository(),
                resourcesManagement,
                ApplicationProvider.getApplicationContext().getContentResolver(),
                StickerPackValidator.getInstance(),
                stickerImageConvertionService);
        stickerPackService = new StickerPackService(resourcesManagement,
                new StickerPackMockRepository(),
                ApplicationProvider.getApplicationContext().getContentResolver(),
                stickerService,
                StickerPackValidator.getInstance(),
                stickerImageConvertionService,
                ApplicationProvider.getApplicationContext().getResources());

        stickerContentProviderReader = new StickerContentProviderReader(stickerPackService,
                resourcesManagement,
                ApplicationProvider.getApplicationContext());
    }

    @Test
    public void testQuery() throws StickerException, IOException {
        stickerPackService.createStickerPack("teste",
                "teste",
                stickerPackImage,
                new OperationCallback<StickerPack>() {
                    @Override
                    public void onCreationFinish(StickerPack createdEntity, StickerException stickerException) {
                        createdStickerPack = createdEntity;
                    }

                    @Override
                    public void onProgressUpdate(int process) {
                    }
                });

        Uri stickerImage = Uri.fromFile(new File("src/test/resources/io/github/miguelteles/beststickerapp/unit/service/test_image.jpg"));

        Sticker sticker1 = createSticker(stickerImage);
        Sticker sticker2 = createSticker(stickerImage);
        Sticker sticker3 = createSticker(stickerImage);
        Sticker sticker4 = createSticker(stickerImage);
        List<Sticker> stickerList = List.of(sticker1, sticker2, sticker3, sticker4);

        List<StickerPack> stickerPacks = stickerContentProviderReader.fetchStickerPacks();

        assertFalse(stickerPacks.isEmpty());
        for (StickerPack stickerPack : stickerPacks) {

            assertEquals(createdStickerPack, stickerPack);
            assertStickerPack(stickerPack);

            Bitmap resizedTrayImageFile = stickerContentProviderReader.fetchAsset(StickerUriProvider.getStickerAsset(stickerPack.getIdentifier(), stickerPack.getResizedTrayImageFile()));
            assertNotNull(resizedTrayImageFile);

            String type = stickerContentProviderReader.getType(StickerUriProvider.getStickerAsset(stickerPack.getIdentifier(), stickerPack.getResizedTrayImageFile()));
            assertNotNull(type);

            type = stickerContentProviderReader.getType(StickerUriProvider.getMetadataCode());
            assertNotNull(type);

            type = stickerContentProviderReader.getType(StickerUriProvider.getStickerListFromPack(stickerPack.getIdentifier()));
            assertNotNull(type);

            type = stickerContentProviderReader.getType(StickerUriProvider.getMetadataCodeFromPack(stickerPack.getIdentifier()));
            assertNotNull(type);

            assertStickerPack(stickerContentProviderReader.fetchStickerPackByIdentifier(stickerPack.getIdentifier()));

            Bitmap originalTrayImageFile = stickerContentProviderReader.fetchAsset(StickerUriProvider.getStickerOriginalAsset(stickerPack.getIdentifier(),  stickerPack.getOriginalTrayImageFile()));
            assertNotNull(originalTrayImageFile);


            for (Sticker stickerFromContentProvider : stickerPack.getStickers()) {
                assertNotNull(stickerFromContentProvider.getIdentifier());
                assertNotNull(stickerFromContentProvider.getStickerImageFile());
                assertTrue(stickerFromContentProvider.getSize() != 0);
                assertNotNull(stickerFromContentProvider.getPackIdentifier());
                assertNotNull(stickerFromContentProvider.getAccessibilityText());
                assertNotNull(stickerFromContentProvider.getEmojis());

                for (Sticker createdSticker : stickerList) {
                    if (createdSticker.getIdentifier().equals(stickerFromContentProvider.getIdentifier())) {
                        assertEquals(stickerFromContentProvider, createdSticker);
                    }
                }

                type = stickerContentProviderReader.getType(StickerUriProvider.getStickerAsset(stickerPack.getIdentifier(), stickerFromContentProvider.getStickerImageFile()));
                assertNotNull(type);

                Bitmap imageStick = stickerContentProviderReader.fetchAsset(StickerUriProvider.getStickerAsset(stickerPack.getIdentifier(), stickerFromContentProvider.getStickerImageFile()));
                assertNotNull(imageStick);
            }
        }
    }

    private static void assertStickerPack(StickerPack stickerPack) {
        assertNotNull(stickerPack.getIdentifier()); //STICKER_PACK_IDENTIFIER_IN_QUERY
        assertNotNull(stickerPack.getName()); //STICKER_PACK_NAME_IN_QUERY
        assertNotNull(stickerPack.getPublisher()); //STICKER_PACK_PUBLISHER_IN_QUERY
        assertNotNull(stickerPack.getResizedTrayImageFile()); //STICKER_PACK_ICON_IN_QUERY
        assertNotNull(stickerPack.getOriginalTrayImageFile()); //STICKER_PACK_ICON_ORIGINAL_IMAGE_FILE
        assertNotNull(stickerPack.getFolderName()); //FOLDER
        assertNotNull(stickerPack.getPublisherEmail()); //PUBLISHER_EMAIL
        assertNotNull(stickerPack.getPublisherWebsite()); //PUBLISHER_WEBSITE
        assertNotNull(stickerPack.getPrivacyPolicyWebsite()); //PRIVACY_POLICY_WEBSITE
        assertNotNull(stickerPack.getLicenseAgreementWebsite()); //LICENSE_AGREEMENT_WEBSITE
        assertNotNull(stickerPack.getImageDataVersion()); //IMAGE_DATA_VERSION
        assertNotNull(stickerPack.getAndroidPlayStoreLink()); //ANDROID_APP_DOWNLOAD_LINK_IN_QUERY
        assertNotNull(stickerPack.getIosAppStoreLink()); //IOS_APP_DOWNLOAD_LINK_IN_QUERY
    }

    private Sticker createSticker(Uri stickerImage) {
        Sticker[] sticker = new Sticker[]{null};
        stickerPackService.createSticker(createdStickerPack,
                stickerImage,
                new OperationCallback<Sticker>() {
                    @Override
                    public void onCreationFinish(Sticker createdEntity, StickerException stickerException) {
                        sticker[0] = createdEntity;
                    }

                    @Override
                    public void onProgressUpdate(int process) {
                    }
                });
        return sticker[0];
    }

    public static class StickerContentProviderReader {

        private ContentProvider contentProvider;

        public StickerContentProviderReader(StickerPackService stickerPackService,
                                            ResourcesManagement resourcesManagement,
                                            Context context) {
            this.contentProvider = new StickerContentProvider(stickerPackService,
                    resourcesManagement,
                    context);
        }

        /**
         * Get the list of sticker packs for the sticker content provider
         */
        @NonNull
        public ArrayList<StickerPack> fetchStickerPacks() throws IllegalStateException {
            final Cursor cursor = contentProvider.query(StickerContentProvider.AUTHORITY_URI, null, null, null, null);
            if (cursor == null) {
                throw new IllegalStateException("could not fetch from content provider, " + BuildConfig.CONTENT_PROVIDER_AUTHORITY);
            }
            final ArrayList<StickerPack> stickerPackList = fetchFromContentProvider(cursor);
            for (StickerPack stickerPack : stickerPackList) {
                stickerPack.setStickers(getStickersForPack(stickerPack));
            }
            return stickerPackList;
        }

        public Bitmap fetchAsset(Uri uri) throws IOException {
            try (ParcelFileDescriptor parcelFileDescriptor = contentProvider.openFile(uri, "r")) {
                return BitmapFactory.decodeStream(new FileInputStream(parcelFileDescriptor.getFileDescriptor()));
            }
        }

        public StickerPack fetchStickerPackByIdentifier(UUID identifier) {
            final Cursor cursor = contentProvider.query(StickerUriProvider.getMetadataCodeFromPack(identifier), null, null, null, null);
            if (cursor == null) {
                throw new IllegalStateException("could not fetch from content provider, " + BuildConfig.CONTENT_PROVIDER_AUTHORITY);
            }
            ArrayList<StickerPack> stickerPacks = fetchFromContentProvider(cursor);
            return stickerPacks.get(0);
        }

        public String getType(Uri uri) {
            return contentProvider.getType(uri);
        }

        @NonNull
        private List<Sticker> getStickersForPack(StickerPack stickerPack) {
            final List<Sticker> stickers = fetchFromContentProviderForStickers(stickerPack.getIdentifier());
            for (Sticker sticker : stickers) {
                final byte[] bytes;
                try {
                    bytes = fetchStickerAsset(stickerPack.getIdentifier(),
                            sticker.getStickerImageFile());
                    if (bytes.length <= 0) {
                        throw new IllegalStateException("Asset file is empty, pack: " + stickerPack.getName() + ", sticker: " + sticker.getStickerImageFile());
                    }
                    sticker.setSize(bytes.length);
                } catch (IOException | IllegalArgumentException e) {
                    throw new IllegalStateException("Asset file doesn't exist. pack: " + stickerPack.getName() + ", sticker: " + sticker.getStickerImageFile(), e);
                }
            }
            return stickers;
        }


        @NonNull
        private ArrayList<StickerPack> fetchFromContentProvider(Cursor cursor) {
            ArrayList<StickerPack> stickerPackList = new ArrayList<>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                final UUID identifier = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_IDENTIFIER_IN_QUERY))); //STICKER_PACK_IDENTIFIER_IN_QUERY
                final String name = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_NAME_IN_QUERY)); //STICKER_PACK_NAME_IN_QUERY
                final String publisher = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_PUBLISHER_IN_QUERY)); //STICKER_PACK_PUBLISHER_IN_QUERY
                final String originalTrayImage = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_ICON_ORIGINAL_IMAGE_FILE)); //STICKER_PACK_ICON_ORIGINAL_IMAGE_FILE
                final String resizedTrayImage = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_ICON_IN_QUERY)); //STICKER_PACK_ICON_IN_QUERY
                final String folder = cursor.getString(cursor.getColumnIndexOrThrow(FOLDER)); //FOLDER
                final String publisherEmail = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_EMAIL)); //PUBLISHER_EMAIL
                final String publisherWebsite = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_WEBSITE)); //PUBLISHER_WEBSITE
                final String privacyPolicyWebsite = cursor.getString(cursor.getColumnIndexOrThrow(PRIVACY_POLICY_WEBSITE)); //PRIVACY_POLICY_WEBSITE
                final String licenseAgreementWebsite = cursor.getString(cursor.getColumnIndexOrThrow(LICENSE_AGREEMENT_WEBSITE)); //LICENSE_AGREEMENT_WEBSITE
                final Integer imageDataVersion = cursor.getInt(cursor.getColumnIndexOrThrow(IMAGE_DATA_VERSION)); //IMAGE_DATA_VERSION
                final boolean avoidCache = cursor.getShort(cursor.getColumnIndexOrThrow(AVOID_CACHE)) > 0; //AVOID_CACHE
                final String androidAppDownloadLinkInQuery = cursor.getString(cursor.getColumnIndexOrThrow(ANDROID_APP_DOWNLOAD_LINK_IN_QUERY));
                final String iosAppDownloadLinkInQuery = cursor.getString(cursor.getColumnIndexOrThrow(IOS_APP_DOWNLOAD_LINK_IN_QUERY));
                final boolean animatedStickerPack = cursor.getShort(cursor.getColumnIndexOrThrow(ANIMATED_STICKER_PACK)) > 0; //
                final StickerPack stickerPack = new StickerPack(identifier,
                        name,
                        publisher,
                        originalTrayImage,
                        resizedTrayImage,
                        folder,
                        publisherEmail,
                        publisherWebsite,
                        privacyPolicyWebsite,
                        licenseAgreementWebsite,
                        imageDataVersion,
                        avoidCache,
                        animatedStickerPack,
                        androidAppDownloadLinkInQuery,
                        iosAppDownloadLinkInQuery);
                stickerPackList.add(stickerPack);
                cursor.moveToNext();
            }

            return stickerPackList;
        }

        @NonNull
        private List<Sticker> fetchFromContentProviderForStickers(UUID stickerPackIdentifier) {
            Uri uri = StickerUriProvider.getStickerListFromPack(stickerPackIdentifier);

            final String[] projection = {STICKER_FILE_NAME_IN_QUERY,
                    STICKER_FILE_EMOJI_IN_QUERY,
                    STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY,
                    STICKER_IDENTIFIER,
                    STICKER_PACK_IDENTIFIER};
            final Cursor cursor = contentProvider.query(uri, projection, null, null, null);
            List<Sticker> stickers = new ArrayList<>();
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    final String imgFile = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_NAME_IN_QUERY));
                    final UUID identifier = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow(STICKER_IDENTIFIER)));
                    final UUID packIdentifier = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_IDENTIFIER)));
                    final String emojis = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_EMOJI_IN_QUERY));
                    final String accessibilityText = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY));
                    stickers.add(new Sticker(imgFile, List.of(emojis), accessibilityText, identifier, packIdentifier));
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
            return stickers;
        }

        /**
         * Busca um asset da pasta (imagem da figurinha ou da capa do sticker pack)
         **/
        private byte[] fetchStickerAsset(@NonNull final UUID identifier,
                                         @NonNull final String stickerImageFileName) throws IOException {
            //o contentResolver.openInputStream vai pro método openAssetFile do contentProvider
            try (final InputStream inputStream = new FileInputStream(contentProvider.openFile(StickerUriProvider.getStickerAsset(identifier, stickerImageFileName), "r").getFileDescriptor());
                 final ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                if (inputStream == null) {
                    throw new IOException("cannot read sticker asset id: " + identifier + "; name: " + stickerImageFileName);
                }
                int read;
                byte[] data = new byte[16384];

                while ((read = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, read);
                }
                return buffer.toByteArray();
            } catch (IOException ex) {
                throw ex;
            }
        }
    }
}
