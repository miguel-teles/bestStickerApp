package io.github.miguelteles.beststickerapp.services.interfaces.operationcallback;

public interface OnProgressUpdate {

    void onProgressUpdate(int process);

    void onProgressUpdate();

    default int calculateProgress(int current) {
        return current+10;
    }

    OnProgressUpdate EMPTY = new OnProgressUpdate() {
        @Override
        public void onProgressUpdate(int process) {

        }

        @Override
        public void onProgressUpdate() {

        }
    };

}
