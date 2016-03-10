/**
 * HamrahPay Android Library...
 * In App Purchase Service
 * www.Hamrahpay.com
 */
package ir.devage.hamrahpay; // Change this line to your package name


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

/*
 * www.HamrahPay.com
 */


/**
 * Pay Request Async class
 * @author HamrahPay.com
 */
class PayRequest extends AsyncTask<String, Integer, JSONObject>{

	private Context mContext;
	public PayRequest(Context context) {
		mContext = context;
	}

	@Override
	protected JSONObject doInBackground(String... params) {
		return 	Hamrahpay.PayRequest(params[0], params[1],mContext);
	}
}

/**
 * Verify Payment Async class
 * @author HamrahPay.com
 */
class VerifyPayment extends AsyncTask<String, Integer, JSONObject>{

	private Context mContext;
	public VerifyPayment(Context context) {
		mContext = context;
	}
	@Override
	protected JSONObject doInBackground(String... params) {
		return 	Hamrahpay.VerifyPayment(params[0],params[1],mContext);
	}
}


//***********************************************************************************

public class Hamrahpay {
	
	static	Context	context;
	static 	String 	response = null;
	static	String	pay_code=null;
	
	JSONArray result = null;
	private static final String STATUS_TAG ="status";
	private static final String PAYCODE_TAG ="pay_code";
	private static final String ERROR_TAG ="error";


	//=================================================================================
	/**
	 * This method sends a pay request to Hamray pay
	 * @param sku , The product sku code
	 * @param device_id	, The device Unique ID
	 * @return	The status code
	 */
	public	static	JSONObject	PayRequest(String	sku,String	device_id,Context context)
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("sku", sku));
		params.add(new BasicNameValuePair("device_id", device_id));
		params.add(new BasicNameValuePair("email", getPrimaryEmailAddress(context)));
		params.add(new BasicNameValuePair("verification_type", context.getString(R.string.verification_type)));
		response = SendData("https://hamrahpay.com/rest-api/pay-request", params);
		if (response!=null)
		{
			try 
			{
				JSONObject 	jsonObj = new JSONObject(response);
				String		status = jsonObj.getString(STATUS_TAG);



				if(!jsonObj.getBoolean(ERROR_TAG))
				{
					pay_code = jsonObj.getString(PAYCODE_TAG);
				}
				else
				{
					pay_code=null;
					if (status.equals("SELLER_BLOCKED"))
						Log.e("JSON", "Seller is blocked");
					else if (status.equals("TRY_AGAIN"))
						Log.e("JSON", "Try Again later");
					else if (status.equals("BAD_PARAMETERS"))
						Log.e("JSON", "Please check the parameters");
				}
				return  jsonObj;
				//Response response= new Response(status,jsonObj.getString("message"));
				//return	response;
			}
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			Log.e("JSON", "Couldn't get any data from the url");
		}
		return	null;
	}
	//=================================================================================
	/**
	 * This method verifies pay code
	 * @param PayCode , pay code
	 * @return the status of verification
	 */
	public	static JSONObject	VerifyPayment(String	PayCode,String sku,Context context)
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("pay_code", PayCode));
		params.add(new BasicNameValuePair("sku", sku));
		params.add(new BasicNameValuePair("verification_type", context.getString(R.string.verification_type)));
		params.add(new BasicNameValuePair("email", getPrimaryEmailAddress(context)));
		params.add(new BasicNameValuePair("device_id", getDeviceID(context)));
		params.add(new BasicNameValuePair("device_model", Build.MODEL));
		params.add(new BasicNameValuePair("device_manufacturer", Build.MANUFACTURER));
		params.add(new BasicNameValuePair("sdk_version", Integer.toString(Build.VERSION.SDK_INT)));
		params.add(new BasicNameValuePair("android_version", Build.VERSION.RELEASE));

		response = SendData("https://hamrahpay.com/rest-api/verify-payment", params);
		if (response!=null)
		{
			try 
			{
				JSONObject 	jsonObj = new JSONObject(response);

				String		status = jsonObj.getString(STATUS_TAG);
				if(jsonObj.getBoolean(ERROR_TAG))
				{
					if (status.equals("INVALID_TRANSACTION"))
						Log.e("JSON", "Invalid Transaction");
					else if (status.equals("TRY_AGAIN"))
						Log.e("JSON", "Try Again later");
					else if (status.equals("BAD_PARAMETERS"))
						Log.e("JSON", "Please check the parameters");
				}
				return jsonObj;
				//Response response= new Response(status,jsonObj.getString("message"));
				//return	response;
			}
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			Log.e("JSON", "Couldn't get any data from the url");
		}
		return	null;
	}
	//=================================================================================
	/**
	 * This method returns pay code 
	 * @return pay code
	 */
	public	static String	getPayCode()
	{
		return	pay_code;
	}
	//=================================================================================
	
	/**
	 * This method returns the device unique ID
	 * Dont forget to set :
	 * <uses-permission android:name="android.permission.READ_PHONE_STATE" />
	 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
	 * in AndroidManifest.xml
	 */
	public static	String	getDeviceID(Context cont)
	{
		String	deviceId;
		TelephonyManager tm = (TelephonyManager) cont.getSystemService(Context.TELEPHONY_SERVICE);
		deviceId= tm.getDeviceId();


		if (deviceId==null)
		{
			WifiManager m_wm = (WifiManager) cont.getSystemService(Context.WIFI_SERVICE);
			String mac_addr = m_wm.getConnectionInfo().getMacAddress();
			deviceId = mac_addr;
			if (deviceId==null)
				deviceId = Secure.getString(cont.getContentResolver(), Secure.ANDROID_ID);
		}


		if ("9774d56d682e549c".equals(deviceId) || deviceId == null)
		{
			deviceId = ((TelephonyManager) cont.getSystemService( Context.TELEPHONY_SERVICE )).getDeviceId();
		}

		return	(deviceId!=null)?deviceId:"DEVICE_ID_ERROR";
	}
	//=================================================================================
	/**
	 * This method sends post data to url
	 * @param url ,url to make request
	 * @param params , parameters to send via POST
	 * @return response data
	 */
    public static	String SendData(String url,List<NameValuePair> params) 
    {
        try 
        {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;
            
            HttpPost httpPost = new HttpPost(url);
            if (params != null) 
            {
                httpPost.setEntity(new UrlEncodedFormEntity(params));
            }
            httpResponse = httpClient.execute(httpPost);
            StatusLine statusLine = httpResponse.getStatusLine();
            int	statusCode= statusLine.getStatusCode();
            if (statusCode==200)
            {
            	httpEntity = httpResponse.getEntity();
                response = EntityUtils.toString(httpEntity);
            }
            else
            {
            	Log.e("JSON", "Failed to open url");
            	return	null;
            }
            
        } 
        catch (UnsupportedEncodingException e) 
        {
            e.printStackTrace();
        } 
        catch (ClientProtocolException e) 
        {
            e.printStackTrace();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return response;
 
    }
	//--------------------------------------------------------------
	
    public static boolean isNetworkAvailable(Context context) {
    	
        ConnectivityManager connectivityManager 
              = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo =  connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    //---------------------------------------------------------------
	public static String	getPrimaryEmailAddress(Context context)
	{
		Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
		String possibleEmail=null;
		Account[] accounts = AccountManager.get(context).getAccounts();
		for (Account account : accounts) {
			if (emailPattern.matcher(account.name).matches()) {
				possibleEmail = account.name;
				break;
			}
		}
		return possibleEmail;
	}
	//---------------------------------------------------------------
	//---------------------------------------------------------------
	public static void	MakePremium(Context context,String sku)
	{
		SharedPreferences prefs = context.getSharedPreferences("hp_premium", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("premium_key_"+sku,getDeviceID(context));
		editor.commit();
	}
	//---------------------------------------------------------------
	public static boolean isPremium(Context context,String sku)
	{
		SharedPreferences prefs = context.getSharedPreferences("hp_premium", Context.MODE_PRIVATE);
		String status = prefs.getString("premium_key_" + sku, "NOT_SET");
		if ( (!status.equals("NOT_SET")) && status.equals(getDeviceID(context)))
			return true;
		else
			return false;

	}
	//---------------------------------------------------------------
	public static boolean Pay(Context context, String sku)
	{
		if(!isNetworkAvailable(context)) {
			Toast.makeText(context, "دستگاه شما به اینترنت متصل نمیباشد . لطفا از صحت اتصال به اینترنت اطمینان حاصل فرمایید.", Toast.LENGTH_LONG).show();
			return false;
		}
		else
		{
			try
			{
				String	DID= getDeviceID(context);
				JSONObject	Result = new PayRequest(context).execute(sku,DID).get();
				if (Result.getString("status").equals("READY_TO_PAY"))
				{
					String	PayCode = Hamrahpay.getPayCode();
					Intent i = new Intent(context, PayActivity.class);
					i.putExtra("PayCode", PayCode);
					i.putExtra("Sku",sku);
					context.startActivity(i);
				}
				else if (Result.getString("status").equals("BEFORE_PAID"))
				{
					// do verify and update your app...
					Toast.makeText(context, "قبلا پرداخت شده است", Toast.LENGTH_SHORT).show();
					JSONObject vry_res = new	VerifyPayment(context).execute(Hamrahpay.getPayCode(),sku).get();
					if (vry_res.getString("status").equals("SUCCESSFUL_PAYMENT"))
					{
						MakePremium(context, sku);
						Toast.makeText(context, "پرداخت با موفقیت انجام گردید. هم اکنون میتوانید از امکانات نرم افزار استفاده نمایید.", Toast.LENGTH_LONG).show();
						return true;
					}
					else
					{
						Toast.makeText(context, Result.getString("message"), Toast.LENGTH_LONG).show();
					}
					return false;
				}
				else
				{
					Toast.makeText(context, Result.getString("message"), Toast.LENGTH_LONG).show();
				}
				//String	Result = new	VerifyPayment().execute("123456").get();
				//Toast.makeText(getApplicationContext(), Result, Toast.LENGTH_LONG).show();
				Toast.makeText(context, DID, Toast.LENGTH_LONG).show();
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}

		}
		return true;
	}
	//---------------------------------------------------------------
}
