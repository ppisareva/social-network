package com.example.polina.socialnetwork;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;


import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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
    public static String HOST = "https://socialnetwork-core-rest.herokuapp.com/";
    private String logInPath = HOST + "user/login";
    private String sighUpPath = HOST + "user/register";
    private String userInfoPath = HOST + "/user/me";
    private String postPath = HOST + "/post";
    private String postGetPath = HOST + "/timeline/";
    private String postLoadPost = "?limit=%d&before=%s";
    private static final String MAIL = "email";
    private static final String PASSWORD = "password";
    private String postGetComment = HOST + "/post/%s/comment";
    private String getPost = HOST + "/post/%s";
    private String getLike = HOST + "/post/%s/like";
    private String deleteEditPost = HOST + "/user/me/post/%s";





    @Override
    public JSONObject logIn(String email, String password) {
        return logInSignUp(email, password, logInPath);
    }

    @Override
    public JSONObject signUp(String email, String password) {
        return logInSignUp(email, password, sighUpPath);
    }

    @Override
    public JSONObject saveProfile(String name, String birthday, String sex, String imageUrl, String imageMiniUrl) {
        JSONObject o = new JSONObject();
        try {
            o.put(Utils.NAME, name);
            o.put(Utils.BIRTHDAY, birthday);
            o.put(Utils.SEX, sex);
            o.put(Utils.PROF_URL, imageUrl);
            o.put(Utils.MINI_PROF_URL, imageMiniUrl);
            return postRequest(o, userInfoPath);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    private JSONObject deleteRequest(String path) {
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpDelete delete = new HttpDelete(path);
            CookieManager cookieManager = CookieManager.getInstance();
            delete.addHeader("Cookie", cookieManager.getCookie(path));
            HttpResponse resp = client.execute(delete);
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

    private JSONObject getRequest( String path) {
        System.err.println("REQUEST PATH: " + path);
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(path);
            CookieManager cookieManager = CookieManager.getInstance();
            get.addHeader("Cookie", cookieManager.getCookie(path));
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

    @Override
    public JSONObject getProfile() {
        return getRequest(userInfoPath);
    }

    @Override
    public JSONObject getLoadPosts( String idUser, int size,  String idPost) {
        if(idPost.isEmpty()){
            return getRequest( postGetPath + idUser);
        }
        return getRequest( postGetPath + idUser + String.format(postLoadPost, size, idPost));
    }

    @Override
    public JSONObject sendComment( String idPost, String comment) {
        JSONObject o = new JSONObject();
        try {
            o.put(Utils.COMMENT, comment);
            postRequest( o, String.format(postGetComment, idPost) );

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public JSONObject getPost( String idPost) {
        return getRequest( String.format(getPost, idPost));
    }

    @Override
    public JSONObject deletePost(String postId) {
        return deleteRequest(String.format(deleteEditPost, postId));
    }

    @Override
    public JSONObject getLike(String idPost) {
        return getRequest(String.format(getLike, idPost));
    }

    @Override
    public JSONObject getComments(String idPost) {
        return getRequest( String.format(postGetComment, idPost));
    }



    @Override
    public JSONObject editComment( String idPost, String idComment, String comment) throws JSONException {
        JSONObject o = new JSONObject();
        o.put(Utils.COMMENT, comment);
        return putRequest( o, String.format(postGetComment, idPost) +"/" +idComment);
    }

    @Override
    public JSONObject deleteComment( String idPost, String idComment) {
        return deleteRequest( String.format(postGetComment, idPost) + "/" + idComment);
    }

    private JSONObject logInSignUp(String email, String password, String path) {
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(path);
            List<NameValuePair> nameValuePairs = new ArrayList(2);
            nameValuePairs.add(new BasicNameValuePair(MAIL, email));
            nameValuePairs.add(new BasicNameValuePair(PASSWORD, password));
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            HttpResponse resp = client.execute(post);
            if (resp.getStatusLine().getStatusCode() < 400) {
                List<Cookie> cookies = client.getCookieStore().getCookies();
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

    private JSONObject postRequest( JSONObject o, String path) {
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(path);
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

    private JSONObject putRequest(JSONObject o, String path) {
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPut post = new HttpPut(path);
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


    public JSONObject newPost( String massage, JSONObject location, String image, String account) {
        JSONObject o = new JSONObject();
        try {
            o.put(Utils.POST_MASSAGE, massage);
            o.put(Utils.POST_LOCATION, location);
            o.put(Utils.POST_IMAGE, image);
            o.put(Utils.POST_ACCOUNT, account);
            return postRequest( o, postPath);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
