package com.example.polina.socialnetwork;

import android.content.Context;
import android.util.Xml;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.amazonaws.org.apache.http.util.EncodingUtils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by polina on 04.06.15.
 */
public class ServerAPI implements API {
    private static String HOST = "https://socialnetwork-core-rest.herokuapp.com/";
    private String logInPath = HOST + "user/login";
    private String sighUpPath = HOST + "user/register";
    private String userInfoPath = HOST + "/user/me";
    private String postPath = HOST + "/post";

    private static final String MAIL = "email";
    private static final String PASSWORD = "password";
    public static final String NAME = "name";
    public static final String BIRTHDAY = "birthday";
    private static final String SEX = "sex";
    public static final String PROF_URL = "profile_url";
    public static final String MINI_PROF_URL = "mini_profile_url";
    public static final String POST_ACCOUNT = "account";
    public static final String POST_LOCATION = "location";
    public static final String POST_IMAGE = "image";
    public static final String POST_ATTACHMENT = "attachment";
    public static final String POST_MASSAGE = "massage";


    @Override
    public JSONObject logIn(String email, String password, Context context) {
        return logInSignUp(email, password, logInPath, context);

    }

    @Override
    public JSONObject signUp(String email, String password, Context context) {
        return logInSignUp(email, password, sighUpPath, context);
    }

    @Override
    public JSONObject saveProfile(String name, String birthday, String sex, String imageUrl, String imageMiniUrl, Context context) {
        JSONObject o = new JSONObject();
        try {
            o.put(NAME, name);
            o.put(BIRTHDAY, birthday);
            o.put(SEX, sex);
            o.put(PROF_URL, imageUrl);
            o.put(MINI_PROF_URL, imageMiniUrl);
            return postRequest(context, o, userInfoPath);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JSONObject getProfile(Context context) {
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(userInfoPath);
            CookieSyncManager.createInstance(context);
            CookieManager cookieManager = CookieManager.getInstance();
            get.addHeader("Cookie", cookieManager.getCookie(userInfoPath));
            HttpResponse resp = client.execute(get);
            if (resp.getStatusLine().getStatusCode() < 400) {
                String s = EntityUtils.toString(resp.getEntity());
                return new JSONObject(s);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private JSONObject logInSignUp(String email, String password, String path, Context context) {
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(path);
            List<NameValuePair> nameValuePairs = new ArrayList(2);

            nameValuePairs.add(new BasicNameValuePair(MAIL, email));
            nameValuePairs.add(new BasicNameValuePair(PASSWORD, password));
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
            HttpResponse resp = client.execute(post);

            if (resp.getStatusLine().getStatusCode() < 400) {
                List<Cookie> cookies = client.getCookieStore().getCookies();
                CookieSyncManager.createInstance(context);
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                cookieManager.removeAllCookie();
                for (Cookie sessionCookie : cookies)
                    if (sessionCookie != null) {
                        String cookieString = sessionCookie.getName() + "=" + sessionCookie.getValue() + "; domain=" + sessionCookie.getDomain();
                        cookieManager.setCookie(sessionCookie.getDomain(), cookieString);
                    }
                CookieSyncManager.getInstance().sync();
                String s = EntityUtils.toString(resp.getEntity());
                return new JSONObject(s);
            } else {
                System.err.println("ERROR: " + resp.getStatusLine());
                return null;
            }

        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        }
    }

    private JSONObject postRequest(Context context, JSONObject o, String path) {
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(path);
            CookieSyncManager.createInstance(context);
            CookieManager cookieManager = CookieManager.getInstance();
            post.addHeader("Cookie", cookieManager.getCookie(path));
            String data = o.toString();

            post.setEntity(new StringEntity(data, HTTP.UTF_8));
            HttpResponse resp = client.execute(post);

            if (resp.getStatusLine().getStatusCode() < 400) {
                String s = EntityUtils.toString(resp.getEntity());
                return new JSONObject(s);
            } else return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject newPost(Context context, String massage, String location, String attachment, String image, String account) {
        JSONObject o = new JSONObject();

        try {
            o.put(POST_MASSAGE, massage);
            o.put(POST_LOCATION, location);
            o.put(POST_ATTACHMENT, attachment);
            o.put(POST_IMAGE, image);
            o.put(POST_ACCOUNT, account);
            return postRequest(context, o, postPath);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
