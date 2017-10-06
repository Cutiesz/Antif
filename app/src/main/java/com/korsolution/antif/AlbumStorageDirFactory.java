package com.korsolution.antif;

import java.io.File;

abstract class AlbumStorageDirFactory {
	public abstract File getAlbumStorageDir(String albumName);
}
