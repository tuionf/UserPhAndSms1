package com.example.userphandsms;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements OnClickListener{
	
	private TextView phone,sms;
	private Button btn_get_phone,btn_get_sms;
	private AsyncQueryHandler asyncQueryHandler;
	private Uri SMS_INBOX = Uri.parse("content://sms/");

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        phone = (TextView) findViewById(R.id.phone);
        sms = (TextView) findViewById(R.id.sms);
        btn_get_phone = (Button) findViewById(R.id.btn_get_phone);
        btn_get_sms = (Button) findViewById(R.id.btn_get_sms);
        btn_get_phone.setOnClickListener(this);
        btn_get_sms.setOnClickListener(this);
        
        asyncQueryHandler = new MyAsyncQueryHandler(getContentResolver());
        
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_get_phone:
//			getPhone();
//			getPhone1();
			getPhone2();
			break;
		case R.id.btn_get_sms:
//			getSms();
			getSmsInPhone();
			break;
		default:
			break;
		}
	}

	private void getPhone2() {
		Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
				null, null, null, null);
		int contactIdIndex = 0;
		int nameIndex = 0;

		if(cursor.getCount() > 0) {
			contactIdIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
			nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
		}
		while(cursor.moveToNext()) {
			String contactId = cursor.getString(contactIdIndex);
			String name = cursor.getString(nameIndex);
			Log.e("hhp", "=contactId=="+contactId+"---"+name);

            /*
             * 查找该联系人的phone信息
             */
			Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
					null, null);
			int phoneIndex = 0;
			if(phones.getCount() > 0) {
				phoneIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
			}
			while(phones.moveToNext()) {
				String phoneNumber = phones.getString(phoneIndex);
				Log.e("hhp", "=phoneNumber=="+phoneNumber);
			}

            /*
             * 查找该联系人的email信息
             */
			Cursor emails = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
					null,
					ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=" + contactId,
					null, null);
			int emailIndex = 0;
			if(emails.getCount() > 0) {
				emailIndex = emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
			}
			while(emails.moveToNext()) {
				String email = emails.getString(emailIndex);
				Log.e("hhp", "=email=="+email);
			}

			Cursor address = getContentResolver().query(
							ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = " + contactId, null, null);
			int addIndex = 0;
			if(address.getCount() > 0) {
				addIndex = address.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS);
			}
			while(address.moveToNext()) {
				String addr = address.getString(addIndex);
				Log.e("hhp", "=addr=="+addr);
			}

			Cursor IMs = getContentResolver().query(
					ContactsContract.Data.CONTENT_URI,
					new String[] { ContactsContract.Data._ID, ContactsContract.CommonDataKinds.Im.PROTOCOL, ContactsContract.CommonDataKinds.Im.DATA },
					ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "='"
							+ ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE + "'",
					new String[] { contactId }, null);

			int imPro = 0;
			int imData = 0;
			if(IMs.getCount() > 0) {
				imPro = IMs.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL);
				imData = IMs.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA);
			}
			while(IMs.moveToNext()) {
				String imPr = IMs.getString(imPro);
				String imDa = IMs.getString(imData);
				Log.e("hhp", "=imPr=="+imPr+"=imDa=="+imDa);
			}

			IMs.close();
			emails.close();
			phones.close();
			address.close();
		}

		cursor.close();

	}

	private void getPhone1() {
		// TODO Auto-generated method stub
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; 
		ContentResolver resolver = getContentResolver();
		String id = "";
		Cursor phoneCursor = resolver.query(uri, null, null, null, null);
		while (phoneCursor.moveToNext()) {
			id = phoneCursor.getString(phoneCursor.getColumnIndex(android.provider.ContactsContract.Contacts._ID));
			String name = phoneCursor.getString(phoneCursor.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME));
			String phone = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			
			Log.e("hhp", id+"==="+name+"==="+phone);
		}

		Cursor emailCursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
				null,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null,null);
		while (emailCursor.moveToNext()){
			String emailType = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
			String emailValue = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

			Log.e("hhp1", emailType+"==="+emailValue+"===");
		}

		Cursor imCursor = resolver.query(ContactsContract.Data.CONTENT_URI,
				new String[] { ContactsContract.Contacts.Data._ID, ContactsContract.CommonDataKinds.Im.PROTOCOL, ContactsContract.CommonDataKinds.Im.DATA },
				ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "='"
						+ ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE + "'",
						new String[] { id }, null);
		while (imCursor.moveToNext()){
			String protocol = imCursor.getString(imCursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL));
			String date = imCursor.getString(imCursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));

			Log.e("hhp2", protocol+"==protocol==date=="+date+"===");
		}


	}

	private void getSms() {
		ContentResolver cr = getContentResolver();
		String[] projection = new String[] { "body" };//"_id", "address", "person",, "date", "type
		String where = " address = '1066321332' AND date >  "
				+ (System.currentTimeMillis() - 10 * 60 * 1000);
		Cursor cur = cr.query(SMS_INBOX, projection, where, null, "date desc");
		if (null == cur)
			return;
		if (cur.moveToNext()) {
			String number = cur.getString(cur.getColumnIndex("address"));//手机号
			String name = cur.getString(cur.getColumnIndex("person"));//联系人姓名列表
			String body = cur.getString(cur.getColumnIndex("body"));
			//这里我是要获取自己短信服务号码中的验证码~~
			Pattern pattern = Pattern.compile(" [a-zA-Z0-9]{10}");
			Matcher matcher = pattern.matcher(body);
			if (matcher.find()) {
				String res = matcher.group().substring(1, 11);
				Log.e("hhp","+++"+res);
			}
		}
	}

	private void getPhone() {
		
		init();
	}
	
	private void init() {
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		String[] projection = { ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",
				ContactsContract.CommonDataKinds.Email.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY };
		asyncQueryHandler.startQuery(0, null, uri, projection, null, null,
				"sort_key COLLATE LOCALIZED asc");
		
	}

	private class MyAsyncQueryHandler extends AsyncQueryHandler{

		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// TODO Auto-generated method stub
			super.onQueryComplete(token, cookie, cursor);
			
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
//					// 名字
//					int nameId = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
//					//电话 
//					int phoneId = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
//					//邮箱
//					int emailId = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
//					//地址
//					int addId = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
//					//IM
//					int imId = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE);
//					
//					cursor.moveToPosition(i);
//					String name = cursor.getString(nameId);
//					String number = cursor.getString(phoneId);
//					String email = cursor.getString(emailId);
//					String add = cursor.getString(addId);
//					String im = cursor.getString(imId);
//					
					cursor.moveToPosition(i);
					String id = cursor.getString(0);
					String name = cursor.getString(1);
					String number = cursor.getString(2);
					String sortKey = cursor.getString(3);
					String emailString = cursor.getString(4);
//					int Emailid = cursor.getInt(4);
//					String emailString = cursor.getString(cursor.getColumnIndex(Email.ADDRESS));
//					int Email = cursor.getInt(4);
					Long photoId = cursor.getLong(5);
					String lookUpKey = cursor.getString(6);

					Log.e("hhp", "id--"+id+"==="+"name " +name+"---"+" phoneid "+number+"---"+"sortKey "+sortKey+"---"+"Email "+emailString+"---"+"photoId "+photoId+"---"+"lookUpKey "+lookUpKey);
//					Log.e("hhp", "phoneid "+number+"---");
//					Log.e("hhp", "sortKey "+sortKey+"---");
//					Log.e("hhp", "contactId "+contactId+"---");
//					Log.e("hhp", "photoId "+photoId+"---");
//					Log.e("hhp", "lookUpKey "+lookUpKey+"---");
				}
				
			}
		}
		
		
	}

	public String getSmsInPhone() {
		final String SMS_URI_ALL = "content://sms/";   //所有信息
		final String SMS_URI_INBOX = "content://sms/inbox";  //收件箱
		final String SMS_URI_SEND = "content://sms/sent";  //已发送
		final String SMS_URI_DRAFT = "content://sms/draft";  //草稿
		final String SMS_URI_OUTBOX = "content://sms/outbox";  //发件箱
		final String SMS_URI_FAILED = "content://sms/failed";  //发送失败
		final String SMS_URI_QUEUED = "content://sms/queued";   //待发送列表
		JSONArray jsonArray  = new JSONArray();
		Map<String,String> map = new LinkedHashMap();
		StringBuilder smsBuilder = new StringBuilder();

		try {
			Uri uri = Uri.parse(SMS_URI_ALL);
			String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" };
			Cursor cur = getContentResolver().query(uri, projection, null, null, "date desc");      // 获取手机内部短信

			if (cur.moveToFirst()) {
				cur.getCount();
				Log.e("hhp","短信---"+cur.getCount());
				int index_Address = cur.getColumnIndex("address");
				int index_Person = cur.getColumnIndex("person");
				int index_Body = cur.getColumnIndex("body");
				int index_Date = cur.getColumnIndex("date");
				int index_Type = cur.getColumnIndex("type");

				do {
					String strAddress = cur.getString(index_Address);
					int intPerson = cur.getInt(index_Person);
					String strbody = cur.getString(index_Body);
					long longDate = cur.getLong(index_Date);
//					long longDate1 = 1111;
					int intType = cur.getInt(index_Type);

//					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					Date d = new Date(longDate);
//					Date ddd = new Date(longDate1);
					Date dd = dateFormat.parse("2017-01-10");
					long longDate1 = dd.getTime();
					Log.e("hhp4",longDate1+"==longDate1=");
					//这个短信时间

					String strDate = dateFormat.format(d);
//					Date dd = dateFormat.parse(strDate);
//					String strDate2 = dateFormat.format(dd);

//					Date d2 = dateFormat.parse(strDate2);
					Log.e("hhp4",longDate+"===");
					Log.e("hhp4",d.toString()+"======");
					Log.e("hhp4",strDate+"=========");
					Log.e("hhp4",dd+"++++++++++++++++++++++++++++++++++++++++");

					String strType = "";
					if (intType == 1) {
						strType = "接收";
					} else if (intType == 2) {
						strType = "发送";
					} else {
						strType = "null";
					}

					//base64编码
					String strbody1 = Base64.encodeToString(strbody.getBytes(),Base64.DEFAULT);

					Log.e("hhp","短信"+"[ "+strAddress + ", "+strbody + ", "+strDate + ",]\n\n");
					smsBuilder.append("[ ");
					smsBuilder.append(strAddress + ", ");
//					smsBuilder.append(intPerson + ", ");
					smsBuilder.append(strbody + ", ");
					smsBuilder.append(strDate + ", ");
//					smsBuilder.append(strType);
					smsBuilder.append(" ]\n\n");

					JSONObject js = new JSONObject();
					JSONObject jsonObject = new JSONObject();

					if (dd.before(d)){
						map.put("phone",strAddress);
						map.put("time",strDate);
						map.put("content",strbody);

						jsonObject.put("phone",strAddress);
						jsonObject.put("time",strDate);
						jsonObject.put("content",strbody);
					}


					//					js.put("msg",jsonObject);
					js.put("msg",new JSONObject(map));

					jsonArray.put(js);

					Log.e("hhp1",jsonObject.toString());
					Log.e("hhp2",js.toString());



				} while (cur.moveToNext());

				if (!cur.isClosed()) {
					cur.close();
					cur = null;
				}
			} else {
				smsBuilder.append("no result!");
			} // end if

			smsBuilder.append("getSmsInPhone has executed!");


		} catch (SQLiteException ex) {

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Log.e("hhp3",jsonArray.toString());
//		Log.e("hhp4",);
		return smsBuilder.toString();
	}
}

