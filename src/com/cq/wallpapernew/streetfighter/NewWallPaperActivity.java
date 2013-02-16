package com.cq.wallpapernew.streetfighter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.airpush.android.Airpush;
import com.pad.android.listener.AdListener;
import com.umeng.analytics.MobclickAgent;

public class NewWallPaperActivity extends Activity implements
		OnMenuItemClickListener {

	// �Ƿ����˹��
	private boolean isClicked = false;
	private boolean isAdShow = false;

	// ��ʾ�Ƿ�Ӧ�ÿɼ�
	private boolean isStop = false;

	private ViewGroup mainlayout;
	// ͼƬ�л���
	private ViewPager myPicPager;
	private MyPagerAdapter myAdapter;

	private ArrayList<Integer> imgSourceIdlist;

	// operation menuitem
	private Menu menu;
	private MenuItem downloadItem;
	private MenuItem setWallPaperItem;
	private MenuItem removeAdItem;

	// ��ʱ���������
	private static final int startads = 200;
	// ��ʾ���
	private static final int LOAD_DISPLAY_TIME = 15000;

	// �����ļ�
	String leadbolt_bannerkey = "487832941";

	// airpush
	Airpush airpush;

	// leadbolt
	private com.pad.android.iappad.AdController bannerController;
	private String leadboltBanner = "487832941";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wall_paper);

		// initUI
		initUIComponents();

		// airpush Notification.
		airpush = new Airpush(this.getApplicationContext());
		airpush.startPushNotification(false);

		// leadbolt banner
		bannerController = new com.pad.android.iappad.AdController(this,
				leadboltBanner, new AdListener() {
					public void onAdLoaded() {
						MobclickAgent.onEvent(NewWallPaperActivity.this,
								UMengEvent.getAds);
					}

					public void onAdClicked() {
						MobclickAgent.onEvent(NewWallPaperActivity.this,
								UMengEvent.clickAds);
						closeAds();
						// ������ʾ
						if (Math.random() <= 0.05) {
							// 5%����ѭ����ʾ
							new Handler().postDelayed(new AdsShowRunnable(),
									LOAD_DISPLAY_TIME);
						}
					}

					public void onAdClosed() {
					}

					public void onAdCompleted() {
					}

					public void onAdFailed() {
						MobclickAgent.onEvent(NewWallPaperActivity.this,
								UMengEvent.getAdFailed);
					}

					public void onAdProgress() {
					}

					public void onAdAlreadyCompleted() {
					}

					public void onAdHidden() {
					} // this function is now deprecated

					public void onAdPaused() {
					}

					public void onAdResumed() {
					}
				});
		bannerController.loadAd();
	}

	@Override
	public void onStop() {
		this.isStop = true;
		super.onStop();
	}

	@Override
	public void onResume() {
		this.isStop = false;
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onRestart() {
		this.isStop = false;
		super.onRestart();
		MobclickAgent.onPause(this);
	}

	class AdsShowRunnable implements Runnable {
		@Override
		public void run() {
			if (isClicked && !isAdShow) {
				// �����,�˳�
				return;
			} else if (!isClicked && !isAdShow) {
				// û�������ûչʾ�������ȴ�
			} else if (!isClicked && isAdShow) {
				// û�����������չʾ
				if (!isStop) {
					showAdHint();
				}
			}
			new Handler().postDelayed(this, LOAD_DISPLAY_TIME);
		}
	}

	/*
	 * add listener for all the UIComponents
	 */
	private void initUIComponents() {
		this.mainlayout = (ViewGroup) this.findViewById(R.id.main_layout);
		loadImageFlipper();
	}

	/*
	 * load pics into flipper
	 */
	public void loadImageFlipper() {
		myPicPager = (ViewPager) findViewById(R.id.myPicPager);

		// ��ʼ��ͼƬ
		this.loadPicSource();
		// ��ʼ��viewPager
		ArrayList<ImageView> list = new ArrayList<ImageView>();
		for (Integer id : this.imgSourceIdlist) {
			list.add(null);
		}
		
		myAdapter = new MyPagerAdapter(this, list, this.imgSourceIdlist);
		myPicPager.setAdapter(myAdapter);
		myPicPager.setCurrentItem(0);
	}

	// ����ͼƬ�¼�
	private OnClickListener imageListener = null;

	public ImageView getImageView(int imageId) {
		ImageView iv = new ImageView(this);
		iv.setImageResource(imageId);
		if (imageListener == null) {
			imageListener = new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					openOptionsMenu();
				}

			};
		}
		iv.setOnClickListener(imageListener);
		return iv;
	}

	/**
	 * װ��ͼƬ��Դ������
	 */
	public void loadPicSource() {
		// �÷���װ��ͼƬ����
		Class cc = R.drawable.class;
		Field[] fields = cc.getFields();
		imgSourceIdlist = new ArrayList<Integer>();
		try {
			for (Field f : fields) {
				String s = f.getName();
				if (s.startsWith("img")) {
					int i = f.getInt(cc);
					imgSourceIdlist.add(i);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d("dataerror", "load data error in ImageData.loadLocalImage");
		}
	}

	/*
	 * �رչ����ͼ
	 */
	private void closeAds() {
		this.bannerController.hideAd();
		this.isClicked = true;
	}

	private String getStringById(int resId) {
		return this.getResources().getString(resId);
	}

	/*
	 * show hint
	 */
	private Toast toast;

	private void showHint(String hint) {
		toast = Toast.makeText(this, hint, Toast.LENGTH_LONG);
		toast.show();
	}

	private void showAdHint() {
		String ads = getText(R.string.click_ads_hint).toString();
		showHint(ads);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		// add menuitem
		this.downloadItem = menu.add(1, 1, 1, getText(R.string.download_menu));
		this.setWallPaperItem = menu.add(1, 2, 2,
				getText(R.string.setwallpaper_menu));
		this.removeAdItem = menu
				.add(1, 3, 3, getText(R.string.remove_ads_menu));

		// set MenuIcon
		downloadItem.setIcon(R.drawable.down);
		setWallPaperItem.setIcon(R.drawable.setaswall);
		removeAdItem.setIcon(R.drawable.removeads);

		// set listener
		downloadItem.setOnMenuItemClickListener(this);
		setWallPaperItem.setOnMenuItemClickListener(this);
		removeAdItem.setOnMenuItemClickListener(this);

		// down
		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if (item == this.downloadItem) {
			MobclickAgent.onEvent(this, UMengEvent.savePic);
			return this.saveSelectImage();
		} else if (item == this.setWallPaperItem) {
			MobclickAgent.onEvent(this, UMengEvent.setWallPaper);
			return this.setWallPaper();
		} else if (item == this.removeAdItem) {
			MobclickAgent.onEvent(this, UMengEvent.clickRemove);
			this.showAdHint();
			return true;
		}

		return true;
	}

	/*
	 * �����ļ�
	 */
	private boolean saveSelectImage() {

		String path = "/mnt/sdcard/" + "wallpaper/";
		File d = new File(path);
		if (!d.exists()) {
			d.mkdirs();
		}

		// write to file
		try {
			// create file
			int current = myPicPager.getCurrentItem();
			String outfile = path + File.separator + "img" + current + ".jpg";
			File f = new File(outfile);
			boolean b = f.createNewFile();

			// saveImage
			FileOutputStream fos = new FileOutputStream(f);
			this.getSelectedBitmap().compress(CompressFormat.JPEG, 100, fos);
			fos.close();

		} catch (Exception e) {
			e.printStackTrace();
			this.showHint("wrong!");
			MobclickAgent.onEvent(NewWallPaperActivity.this,
					UMengEvent.saveFailed);
			return false;
		}
		// show hint
		this.showHint(this.getResources().getString(R.string.download_ok_hint)
				+ ":" + path);
		MobclickAgent
				.onEvent(NewWallPaperActivity.this, UMengEvent.saveSuccess);
		return true;
	}

	/*
	 * �õ�ѡ�е�Bitmap
	 */
	private Bitmap getSelectedBitmap() {
		int position = this.myPicPager.getCurrentItem();
		int resId = this.imgSourceIdlist.get(position);

		InputStream in = this.getResources().openRawResource(resId);
		BitmapDrawable dr = new BitmapDrawable(in);
		Bitmap bitmap = dr.getBitmap();
		return bitmap;
	}

	/*
	 * ���óɱ�ֽ
	 */
	private boolean setWallPaper() {

		try {
			WallpaperManager wallpaperManager = WallpaperManager
					.getInstance(this);
			wallpaperManager.setResource(this.imgSourceIdlist.get(myPicPager
					.getCurrentItem()));
		} catch (IOException e) {
			this.showHint("wrong!");
			MobclickAgent.onEvent(NewWallPaperActivity.this,
					UMengEvent.setFailed);
		}

		this.showHint(getStringById(R.string.set_ok_hint));
		MobclickAgent.onEvent(NewWallPaperActivity.this, UMengEvent.setSuccess);
		return true;
	}

	// �����˳���
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		MobclickAgent.onEvent(this, UMengEvent.clickBack);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// �˳���ť
			this.outDialog();
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			super.openOptionsMenu();
		}
		super.onKeyDown(keyCode, event);
		return true;
	}

	// �˳��Ի���
	private void outDialog() {
		Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Exit");
		builder.setSingleChoiceItems(R.array.exit, 0,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface,
							int which) {
						String url = null;
						Intent intent = null;
						switch (which) {
						case 0:// ���
							MobclickAgent.onEvent(NewWallPaperActivity.this,
									UMengEvent.clickRate);
							String pname = NewWallPaperActivity.class
									.getPackage().getName();
							url = "market://details?id=" + pname;
							intent = new Intent(Intent.ACTION_VIEW);
							intent.setData(Uri.parse(url));
							startActivity(intent);

							break;
						case 1:// �õ�����APP
							airpush.startSmartWallAd();
							break;
						case 2:// �˳�
							new AlertDialog.Builder(NewWallPaperActivity.this)
									.setTitle("Exit")
									.setMessage("thanks! Rate this APP?")
									.setPositiveButton(
											"Yes",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialoginterface,
														int i) {
													// ȷ����ȥ����
													String pname = NewWallPaperActivity.class
															.getPackage()
															.getName();
													String url = "market://details?id="
															+ pname;
													Intent intent = new Intent(
															Intent.ACTION_VIEW);
													intent.setData(Uri
															.parse(url));
													startActivity(intent);
												}
											})
									.setNegativeButton(
											"Exit",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialoginterface,
														int i) {
													finish();
													Intent intent = new Intent(Intent.ACTION_MAIN);
													intent.addCategory(Intent.CATEGORY_HOME);
													intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
													startActivity(intent);
												}
											}).show();
							break;
						case 3:// ȡ��
							dialogInterface.dismiss();
							break;
						}
					}
				}).show();
	}

}
