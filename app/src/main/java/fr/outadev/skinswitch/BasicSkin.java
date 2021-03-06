/*
 * SkinSwitch - BasicSkin
 * Copyright (C) 2014-2015  Baptiste Candellier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.outadev.skinswitch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import fr.outadev.skinswitch.network.ChallengeRequirementException;
import fr.outadev.skinswitch.network.InvalidMojangCredentialsException;
import fr.outadev.skinswitch.network.MojangConnectionHandler;
import fr.outadev.skinswitch.network.UsersManager;

/**
 * Represents a stored skin, as it is in the database.
 *
 * @author outadoc
 */
public abstract class BasicSkin implements Serializable {

	private static final long serialVersionUID = 7477747633546767683L;

	private int id;
	private String name;
	private String description;
	private Date creationDate;
	private Model model;

	/**
	 * Creates a new skin.
	 *
	 * @param id           the skin ID.
	 * @param name         the name of the skin.
	 * @param description  the description of the skin.
	 * @param creationDate the date of creation of the skin.
	 */
	public BasicSkin(int id, String name, String description, Date creationDate) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.creationDate = creationDate;
		this.model = Model.STEVE;
	}

	/**
	 * Similar to {@link #BasicSkin(int, String, String, Date) Skin(int, String,
	 * String, Date)}, except the ID is automatically set to -1.
	 *
	 * @param name         the name of the skin.
	 * @param description  the description of the skin.
	 * @param creationDate the date of creation of the skin.
	 */
	public BasicSkin(String name, String description, Date creationDate) {
		this(-1, name, description, creationDate);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public String getModelString() {
		switch(model) {
			case ALEX:
				return "alex";
			case STEVE:
			default:
				return "steve";
		}
	}

	public void setModelString(String model) {
		if(model.equals("alex")) {
			this.model = Model.ALEX;
		} else {
			this.model = Model.STEVE;
		}
	}

	/**
	 * Gets the path on the filesystem of the raw skin image (which is sent to
	 * minecraft.net).
	 *
	 * @param context required to get the files directory of the app.
	 * @return the absolute path of the raw skin image (doesn't have to actually
	 * exist).
	 */
	protected String getRawSkinPath(Context context) {
		return context.getFilesDir() + "/" + "raw_" + id + ".png";
	}


	/**
	 *
	 * Path getters
	 *
	 */

	/**
	 * Gets the path on the filesystem of the head preview image.
	 *
	 * @param context required to get the files directory of the app.
	 * @return the absolute path of the head image (doesn't have to actually
	 * exist).
	 */
	protected String getSkinHeadPath(Context context) {
		return context.getCacheDir() + "/" + "head_" + id + ".png";
	}

	/**
	 * Gets the path on the filesystem of the front skin preview image.
	 *
	 * @param context required to get the files directory of the app.
	 * @return the absolute path of the front skin image (doesn't have to
	 * actually exist).
	 */
	protected String getFrontSkinPreviewPath(Context context) {
		return context.getCacheDir() + "/" + "preview_front_" + id + ".png";
	}

	/**
	 * Gets the path on the filesystem of the back skin preview image.
	 *
	 * @param context required to get the files directory of the app.
	 * @return the absolute path of the back skin image (doesn't have to
	 * actually exist).
	 */
	protected String getBackSkinPreviewPath(Context context) {
		return context.getCacheDir() + "/" + "preview_back_" + id + ".png";
	}

	/**
	 * Reads a bitmap from the filesystem at the specified path.
	 *
	 * @param path    the path of the bitmap to decode.
	 * @param context
	 * @return the decoded bitmap.
	 * @throws FileNotFoundException if no file could be found at that path.
	 */
	protected Bitmap readBitmapFromFileSystem(String path, Context context) throws FileNotFoundException {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		options.inScaled = false;

		Bitmap bitmap = BitmapFactory.decodeFile(path, options);

		if(bitmap == null) {
			throw new FileNotFoundException("Could not find skin file for ID #" + id + " (path: " + path + ")");
		}

		return bitmap;
	}

	/**
	 *
	 * Filesystem read/write methods
	 *
	 */

	/**
	 * Writes a bitmap to the filesystem at the specified path.
	 *
	 * @param bitmap the bitmap to write.
	 * @param path   the path at which the bitmap will be written.
	 * @throws IOException if an error occurred.
	 */
	protected void writeBitmapToFileSystem(Bitmap bitmap, String path) throws IOException {
		FileOutputStream fos = new FileOutputStream(path);
		bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
		fos.close();
	}

	/**
	 * Gets the raw skin File object for this skin (can be sent to
	 * minecraft.net).
	 *
	 * @param context
	 * @return a File object pointing to the skin's PNG file.
	 */
	public File getRawSkinFile(Context context) {
		return new File(getRawSkinPath(context));
	}

	/**
	 *
	 * Bitmap getters
	 *
	 */

	/**
	 * Gets a bitmap of the raw skin image.
	 *
	 * @param context
	 * @return a bitmap containing the raw skin.
	 * @throws FileNotFoundException if the raw skin wasn't set yet.
	 */
	public Bitmap getRawSkinBitmap(Context context) throws FileNotFoundException {
		return readBitmapFromFileSystem(getRawSkinPath(context), context);
	}

	/**
	 * Gets a bitmap of the skin head image.
	 *
	 * @param context
	 * @return a bitmap containing the skin head.
	 * @throws FileNotFoundException if the raw skin wasn't set yet.
	 */
	public Bitmap getSkinHeadBitmap(Context context) throws FileNotFoundException {
		try {
			return readBitmapFromFileSystem(getSkinHeadPath(context), context);
		} catch(FileNotFoundException e) {
			Log.d(Utils.TAG, "creating head preview and cache for " + this);

			Bitmap bmpPrev = getFrontSkinPreview(context);
			Bitmap bmpHead = SkinRenderer.getCroppedHead(bmpPrev);

			bmpPrev.recycle();

			try {
				saveSkinHeadBitmap(context, bmpHead);
			} catch(IOException ignored) {
			}

			return bmpHead;
		}
	}

	/**
	 * Gets a bitmap of the front skin preview image.
	 *
	 * @param context
	 * @return a bitmap of the front skin preview.
	 * @throws FileNotFoundException if the raw skin wasn't set yet.
	 */
	public Bitmap getFrontSkinPreview(Context context) throws FileNotFoundException {
		try {
			return readBitmapFromFileSystem(getFrontSkinPreviewPath(context), context);
		} catch(FileNotFoundException e) {
			Log.d(Utils.TAG, "creating front preview and cache for " + this);

			Bitmap bmpRaw = getRawSkinBitmap(context);
			Bitmap bmpPrev = SkinRenderer.getSkinPreview(bmpRaw, SkinRenderer.Side.FRONT, 19, model);

			bmpRaw.recycle();

			try {
				saveFrontSkinPreviewBitmap(context, bmpPrev);
			} catch(IOException ignored) {
			}

			return bmpPrev;
		}
	}

	/**
	 * Gets a bitmap of the back skin preview image.
	 *
	 * @param context
	 * @return a bitmap of the back skin preview.
	 * @throws FileNotFoundException if the raw skin wasn't set yet.
	 */
	public Bitmap getBackSkinPreview(Context context) throws FileNotFoundException {
		try {
			return readBitmapFromFileSystem(getBackSkinPreviewPath(context), context);
		} catch(FileNotFoundException e) {
			Log.d(Utils.TAG, "creating back preview and cache for " + this);

			Bitmap bmpRaw = getRawSkinBitmap(context);
			Bitmap bmpPrev = SkinRenderer.getSkinPreview(bmpRaw, SkinRenderer.Side.BACK, 19, model);

			bmpRaw.recycle();

			try {
				saveBackSkinPreviewBitmap(context, bmpPrev);
			} catch(IOException ignored) {
			}

			return bmpPrev;
		}
	}

	/**
	 * Writes a raw skin bitmap to the filesystem.
	 *
	 * @param context
	 * @param bitmap  the bitmap to write.
	 * @throws IOException if an error occured when writing.
	 */
	public void saveRawSkinBitmap(Context context, Bitmap bitmap) throws IOException {
		writeBitmapToFileSystem(bitmap, getRawSkinPath(context));
	}

	/**
	 *
	 * Bitmap setters
	 *
	 */

	/**
	 * Writes a skin head bitmap to the filesystem.
	 *
	 * @param context
	 * @param bitmap  the bitmap to write.
	 * @throws IOException if an error occured when writing.
	 */
	public void saveSkinHeadBitmap(Context context, Bitmap bitmap) throws IOException {
		writeBitmapToFileSystem(bitmap, getSkinHeadPath(context));
	}

	/**
	 * Writes a front skin preview bitmap to the filesystem.
	 *
	 * @param context
	 * @param bitmap  the bitmap to write.
	 * @throws IOException if an error occured when writing.
	 */
	public void saveFrontSkinPreviewBitmap(Context context, Bitmap bitmap) throws IOException {
		writeBitmapToFileSystem(bitmap, getFrontSkinPreviewPath(context));
	}

	/**
	 * Writes a back skin preview bitmap to the filesystem.
	 *
	 * @param context
	 * @param bitmap  the bitmap to write.
	 * @throws IOException if an error occured when writing.
	 */
	public void saveBackSkinPreviewBitmap(Context context, Bitmap bitmap) throws IOException {
		writeBitmapToFileSystem(bitmap, getBackSkinPreviewPath(context));
	}

	/**
	 * Downloads and stores this skin's raw skin on the filesystem.
	 * Source must be specified via {@link #setSource(String).
	 *
	 * @param context
	 * @throws NetworkException if the skin couldn't be downloaded.
	 * @throws IOException      if the skin couldn't be saved.
	 */
	public abstract void downloadSkinFromSource(Context context) throws HttpRequest.HttpRequestException, IOException;

	/**
	 * Checks if the skin's source is a valid skin.
	 * May also download additional info if needed and available.
	 *
	 * @param param may be used as additional info to validate the source
	 * @return true if it's valid, false if it's not
	 */
	public abstract boolean validateSource(String param) throws InvalidSkinSizeException;

	/**
	 * Checks if the skin's source is a valid skin.
	 * May also download additional info if needed and available.
	 *
	 * @return true if it's valid, false if it's not
	 */
	public boolean validateSource() throws InvalidSkinSizeException {
		return validateSource(null);
	}

	private boolean deleteFile(String path) {
		File file = new File(path);
		return file.delete();
	}

	public void deleteAllCacheFilesFromFilesystem(Context context) {
		deleteFile(getSkinHeadPath(context));
		deleteFile(getBackSkinPreviewPath(context));
		deleteFile(getFrontSkinPreviewPath(context));

		Log.i(Utils.TAG, "deleted all cache files for " + this);
	}

	public void deleteAllSkinResFromFilesystem(Context context) {
		deleteAllCacheFilesFromFilesystem(context);
		deleteFile(getRawSkinPath(context));

		Log.i(Utils.TAG, "deleted all local res files for " + this);
	}

	/**
	 * Initiates the upload of this skin.
	 * Asks a confirmation to the user, if this option is enabled, before starting the network operation
	 * and notifying the listener of the imminent transfer.
	 *
	 * @param activity       a context
	 * @param loadingHandler the listener that will be notified of the upload
	 */
	public void initSkinUpload(final Activity activity, final OnSkinLoadingListener loadingHandler) {
		UsersManager usersManager = new UsersManager(activity);

		//if the user isn't logged in, pop up the login window
		if(!usersManager.isLoggedInSuccessfully()) {
			Intent intent = new Intent(activity, MojangLoginActivity.class);
			activity.startActivity(intent);
			return;
		}

		boolean noConfirmation = PreferenceManager.getDefaultSharedPreferences(activity)
				.getBoolean("pref_no_confirmation", false);

		//else, ask for a confirmation, but only if we want to
		if(noConfirmation) {
			uploadSkinAsync(activity, loadingHandler);
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle(activity.getResources().getString(R.string.replace_skin_title,
					getName())).setMessage(activity.getResources().getString(R.string.replace_skin_message, getName()));

			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int id) {
					uploadSkinAsync(activity, loadingHandler);
				}

			});

			builder.setNegativeButton(R.string.no, null);
			builder.create().show();
		}

	}

	/**
	 * Actually uploads the skin, without asking for confirmation.
	 * Notifies the listener of the transfer status.
	 *
	 * @param context        a context
	 * @param loadingHandler the listener that will be notified of the upload
	 */
	private void uploadSkinAsync(final Activity activity, final OnSkinLoadingListener loadingHandler) {
		(new AsyncTask<Void, Void, Exception>() {

			@Override
			protected void onPreExecute() {
				loadingHandler.setLoading(true);
			}

			@Override
			protected Exception doInBackground(Void... voids) {
				MojangConnectionHandler handler = new MojangConnectionHandler(activity);
				UsersManager um = new UsersManager(activity);

				try {
					handler.loginWithCredentials(um.getUser());
					handler.uploadSkinToMojang(BasicSkin.this, activity);
				} catch(Exception e) {
					return e;
				}

				return null;
			}

			@Override
			protected void onPostExecute(Exception e) {
				if(e != null) {
					//display the error if any
					Snackbar.with(activity)
							.text(R.string.error_skin_upload)
							.actionLabel(R.string.error_retry)
							.actionColorResource(R.color.colorAccent)
							.actionListener(new ActionClickListener() {

								@Override
								public void onActionClicked() {
									uploadSkinAsync(activity, loadingHandler);
								}

							})
							.show(activity);

					//if the user needs to fill in a challenge
					if(e instanceof ChallengeRequirementException) {
						Intent intent = new Intent(activity, MojangLoginActivity.class);
						intent.putExtra("step", MojangLoginActivity.Step.CHALLENGE);
						activity.startActivity(intent);
					} else if(e instanceof InvalidMojangCredentialsException) {
						//if the user needs to relog in
						Intent intent = new Intent(activity, MojangLoginActivity.class);
						activity.startActivity(intent);
					}

				} else {
					Snackbar.with(activity)
							.text(R.string.success_skin_upload)
							.show(activity);
				}

				loadingHandler.setLoading(false);
			}

		}).execute();
	}

	@Override
	public String toString() {
		return "Skin [id=" + id + ", name=" + name + ", description=" + description + ", creationDate=" + creationDate + ", " +
				"model=" + model + "]";
	}

	public static enum Model {
		STEVE, ALEX
	}

}
