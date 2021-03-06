package it.polito.ai.ifttt.progetto.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import it.polito.ai.ifttt.progetto.models.Users;
import it.polito.ai.ifttt.progetto.models.requestClass;
import it.polito.ai.ifttt.progetto.models.returnClass;
import it.polito.ai.ifttt.progetto.services.LoginManager;
import twitter4j.DirectMessage;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

@Controller
@RequestMapping("/twitter")
public class TwitterController {

	public static final String tw_appid = "quh3sSXVjZsPJD0858lbzk1ch";
	public static final String tw_appsecret = "4B2XJn3D0lCkVuFoQf3fY3P1oEsHV5GRDH1IlYKPnuY2ilWm8h";
	public static final String redirect_uri = "http://localhost:8080/progetto/api/twitter/tw.do";

	Twitter twitter = null;
	Users user = null;
	Authentication auth = null;
	
	String nextPath = null;
	
	@Autowired
	LoginManager loginManager;

	@RequestMapping(value = "/tw.do", method = RequestMethod.GET)
	public RedirectView connectTwitter() {
		// The factory instance is re-useable and thread safe.
		// Twitter twitter = TwitterFactory.getSingleton();
		// twitter.setOAuthConsumer(tw_appid, tw_appsecret);

		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(tw_appid);
		builder.setOAuthConsumerSecret(tw_appsecret);
		Configuration configuration = builder.build();
		TwitterFactory factory = new TwitterFactory(configuration);
		this.twitter = factory.getInstance();
		
		auth = SecurityContextHolder.getContext().getAuthentication();
	//	String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
	//	this.user = loginManager.findUserByUsername(username);

		RequestToken requestToken = null;
		try {
			requestToken = twitter.getOAuthRequestToken();
		} catch (TwitterException e1) {

			e1.printStackTrace();
		}
		return new RedirectView(requestToken.getAuthorizationURL());
	}

	@RequestMapping(value = "/tw.token", method = RequestMethod.GET, params = { "oauth_token", "oauth_verifier" })
	public RedirectView oauth2Callback(@RequestParam(value = "oauth_token") String oauth_token,
			@RequestParam(value = "oauth_verifier") String oauth_verifier) {

		AccessToken accessToken = null;
		try {
			accessToken = twitter.getOAuthAccessToken(oauth_verifier);
			// System.out.println(accessToken);						
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		// save accessToken.getToken o getSecretToken o oauth_verifier
		if(this.auth !=null) {
			SecurityContextHolder.getContext().setAuthentication(auth);
			String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
			System.out.println(username);		
			this.user = loginManager.findUserByUsername(username);
			loginManager.setTwitterCredentials(user, accessToken.getToken(), accessToken.getTokenSecret());
			
		}

		String path = "http://localhost:8080/progetto/#/"+this.nextPath;
		System.out.println(path);
		return new RedirectView(path);
	}
	
	@RequestMapping(value = "requestTwitter", method = RequestMethod.POST)
	@ResponseBody returnClass checkGoogleConnection(@RequestBody requestClass data) {
		
		System.out.println(data.getUrlNext());		
		
		this.nextPath = data.getUrlNext(); 
				
		String ret = null;
		
		String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		Boolean connected = loginManager.checkTwitterConnection(username);
		
		if(connected == true) {
			ret = "true";
		}
		else {
			ret = "false";
		}
		
		returnClass res = new returnClass();
		res.setAuthenticated(ret);
		return res;
	}

}
