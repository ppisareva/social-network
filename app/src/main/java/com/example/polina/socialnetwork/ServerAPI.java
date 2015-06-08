package com.example.polina.socialnetwork;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by polina on 04.06.15.
 */
public class ServerAPI implements API {
    private static  String HOST = "https://socialnetwork-core-rest.herokuapp.com/";
    private String logInPath = HOST + "user/login";
    private String sighUpPath = HOST + "user/register";
    private String userInfoPath = HOST + "/user/me";
    private static final String MAIL = "email";
    private static final String PASSWORD = "password";
    private static final String NAME = "name";
    private static final String BIRTHDAY = "birthday";
    private static final String SEX = "sex";
    private static final String PROF_URL = "profile_url";
    private static final String MINI_PROF_URL = "mini_profile_url";




    @Override
    public JSONObject logIn(String email, String password,  Context context) {
        return logInSignUp(email, password, logInPath, context);

    }

    @Override
    public JSONObject signUp(String email, String password, Context context) {
        return logInSignUp(email, password, sighUpPath, context);
    }

    @Override
    public JSONObject saveProfile(String name, String birthday, String sex, String imageUrl, String imageMiniUrl, Context context)  {
        JSONObject o = new JSONObject();
        try {
            o.put(NAME, name);
            o.put(BIRTHDAY, birthday);
            o.put(SEX, sex);
            o.put(PROF_URL, imageUrl);
            o.put(MINI_PROF_URL, imageMiniUrl);

        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(userInfoPath);
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        post.addHeader("Cookie", cookieManager.getCookie(userInfoPath));
        String data = o.toString();

            post.setEntity(new StringEntity(data));
            HttpResponse resp = client.execute(post);

            if (resp.getStatusLine().getStatusCode() < 400) {
                String s = EntityUtils.toString(resp.getEntity());
                return new JSONObject(s);
            } else {
                System.err.println("ERROR: " + resp.getStatusLine());
                return null;
            }
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

    private JSONObject logInSignUp (String email, String password, String path, Context context){
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(path);
            List<NameValuePair> nameValuePairs = new ArrayList(2);
            nameValuePairs.add(new BasicNameValuePair(MAIL, email));
            nameValuePairs.add(new BasicNameValuePair(PASSWORD, password));
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse resp = client.execute(post);

            if (resp.getStatusLine().getStatusCode() < 400) {
                List<Cookie> cookies = client.getCookieStore().getCookies();
                CookieSyncManager.createInstance(context);
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                cookieManager.removeAllCookie();
                for (Cookie sessionCookie: cookies)
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


}
