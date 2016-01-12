package ir.devage.hamrahpay;

import java.util.concurrent.ExecutionException;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.ProgressDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class PayActivity extends Activity {
	WebView browser;
	String	PayCode;
	String	sku;
	TextView	urlTextView;
	ProgressDialog progressDialog = null;

	 protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.pay_activity);
		 urlTextView = (TextView) findViewById(R.id.url_txt);
		 Intent i = getIntent();
		 PayCode = i.getStringExtra("PayCode");
		 sku = i.getStringExtra("Sku");
		 String PayURL = "https://hamrahpay.com/cart/app/pay_v2/"+PayCode;
		 browser= (WebView) findViewById(R.id.pay_webview);
		 browser.clearCache(true);
		 startWebView(PayURL);
	 }
	 //------------------------------------------------------------------
	 //------------------------------------------------------------------
	 private void startWebView(String url) {


		 		//
		 		browser.setWebViewClient(new WebViewClient() {


					@Override
					public void onPageStarted(WebView view, String url, Bitmap favicon) {
						if (progressDialog == null) {
							//ProgressDialog.show(PayActivity.this, "لطفا چند لحظه صبر نمایید...", "در حال بارگذاری صفحه پرداخت امن...");
							progressDialog = new ProgressDialog(PayActivity.this);
							progressDialog.setTitle("هدایت به صفحه پرداخت امن...");
							progressDialog.setMessage("لطفا چند لحظه صبر نمایید...");
							progressDialog.show();
						}
						super.onPageStarted(view, url, favicon);
					}

					@Override
					public boolean shouldOverrideUrlLoading(WebView view, String url) {
						if (url.contains("exit_page")) {
							onBackPressed();
							return true;
						}
						view.loadUrl(url);
						urlTextView.setText(url);

						super.shouldOverrideUrlLoading(view,url);
						return false;
					}

					@Override
					public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
						if (errorCode == -2) {
							String errorMessage = "<html><head><meta charset=\"utf-8\" /><style>body{font-family:tahoma;font-size:13px;directin:rtl;text-align:right}</style></head><body><div style=\"color: #a94442;background-color: #f2dede;border-color: #ebccd1;margin:5px; padding:8px\">متاسفانه اشکالی در ارتباط با بانک به وجود آمده است . لطفا دقایقی دیگر مجددا تلاش بفرمایید.</div></body></html>";
							view.loadData(errorMessage, "text/html", "utf-8");
							return;
						}
						super.onReceivedError(view, errorCode, description, failingUrl);
					}


					@Override
					public void onReceivedSslError(WebView view, SslErrorHandler handler,
												   SslError error) {
						//super.onReceivedSslError(view, handler, error);
						handler.proceed();
					}

					//Show loader on url load
					@Override
					public void onPageFinished(WebView view, String url) {
						try {
							if (progressDialog.isShowing()) {
								progressDialog.dismiss();
								progressDialog = null;
								urlTextView.setText(url);
								super.onPageFinished(view, url);
								browser.setVisibility(View.GONE);
								browser.setVisibility(View.VISIBLE);
							}
						} catch (Exception exception) {
							Log.d("progress error",exception.toString());
						}
					}

				});
		 	// Do not disable this line , because it used when user will redirect to bank
		 	browser.getSettings().setDomStorageEnabled(true);
		 	browser.getSettings().setJavaScriptEnabled(true); 
		 	browser.getSettings().setDefaultTextEncodingName("utf-8");
	        browser.loadUrl(url);

	    }
	 //------------------------------------------------------------------
	 /**
	  * Verify Payment after doing payment
	  */
	 @Override
	protected void onDestroy() {
		// Verify Payment here
		 try {
			JSONObject result = new	VerifyPayment(this).execute(PayCode,sku).get();
			if (result.getString("status").equals("SUCCESSFUL_PAYMENT"))
			{
				Hamrahpay.MakePremium(getApplicationContext(),sku);
				Toast.makeText(getApplicationContext(), "پرداخت با موفقیت انجام گردید. هم اکنون میتوانید از امکانات نرم افزار استفاده نمایید.", Toast.LENGTH_LONG).show();
			}
			else
			{
				Toast.makeText(getApplicationContext(), result.getString("message"), Toast.LENGTH_LONG).show();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		 catch (JSONException e) {
			 e.printStackTrace();
		 }
		 
		super.onDestroy();
	}

}
