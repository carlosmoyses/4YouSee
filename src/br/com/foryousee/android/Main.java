package br.com.foryousee.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import br.com.foryousee.android.util.HttpManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.support.v4.app.NavUtils;

public class Main extends Activity 
{
	private boolean debug = true;
	private final String _TAG = "Main"; 
	private FrameLayout mainFrame;
	private String url = "http://validacao.4yousee.com.br/common/xml/layout.xml";
	private HttpManager http;
	private String httpResp;
	private String xmlToParse;
	private Hashtable <Integer, String> screens;
	private int index = -1;
	private Display display;
	private Point point;
	
    @Override
    public void onCreate (Bundle savedInstanceState) 
    {
    	super.onCreate (savedInstanceState);
    	mainFrame = new FrameLayout (this);
    	this.setContentView (mainFrame);
    	initComponents();
    }
    
    /**
     * Método para inicializar os objetos e "parsear" o xml lido de um HTTP.
     */
    private void initComponents()
    {
    	display = getWindowManager().getDefaultDisplay();
    	point = new Point();
    	display.getSize (point);
    	if (debug) Log.d (_TAG, "X: " + point.x + " | Y: " + point.y);
    	screens = new Hashtable <Integer, String> ();
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy (policy);
    	connect();
    	if (xmlToParse != null)
    	{
    		parseXml();
    		initLayout();
    	}
    }
    
    /**
     * Método para fazer uma conexão HTTP e receber um xml como resposta.
     */
    private void connect()
    {
    	http = HttpManager.getInstance();
    	try 
    	{
    		if (debug) Log.d (_TAG, "Conectando a: " + url);
    		httpResp = http.send (url, "teste");
    		if (debug) Log.d (_TAG, "Recebido: " + httpResp);
    		if (httpResp != null && httpResp.length() > 0)
    		{
    			xmlToParse = httpResp;
    		}
		} 
    	catch (IOException e) 
    	{
    		if (debug) Log.e (_TAG, "Erro no HTTP: " + e.getMessage());
		}
    }
    
    /**
     * Método para extrair as informações contidas no xml lido e transforma-las em várias "entradas" para a tabela hash
     * para, posteriormente, serem traduzidas em informações de layouts de tela.
     */
    private void parseXml()
    {
    	String xmlTemp = xmlToParse;
    	xmlTemp = xmlTemp.replaceAll ("/>", "></layout>");
    	String indexAux = xmlTemp.substring (xmlTemp.indexOf ("layouts default=\"") + "layouts default=\"".length(), 
    			xmlTemp.indexOf ("layouts default=\"") + "layouts default=\"".length() + 2);
    	if (debug) Log.d (_TAG, "Índice recebido: " + indexAux);
    	if (indexAux.indexOf ("\"") != -1)
    	{
    		indexAux = indexAux.substring (0, 1);
    	}
    	index = Integer.parseInt (indexAux);
    	if (debug) Log.d (_TAG, "Índice de telea extraído: " + index);
    	while (xmlTemp.indexOf ("<layout") != -1)
    	{
    		String extracted = null;
			extracted = xmlTemp.substring (xmlTemp.indexOf ("<layout "), xmlTemp.indexOf ("</layout>") + "</layout>".length());
			indexAux = xmlTemp.substring (xmlTemp.indexOf ("<layout id=\"") + "<layout id=\"".length(), 
	    			xmlTemp.indexOf ("layout id=\"") + "layout id=\"".length() + 2);
	    	if (indexAux.indexOf ("\"") != -1)
	    	{
	    		indexAux = indexAux.substring (0, 1);
	    	}
	    	int indexToHash = Integer.parseInt (indexAux);
	    	if (debug) Log.d (_TAG, "Índice a ser gravado na tabela hash: " + indexToHash);
	    	screens.put (indexToHash, extracted);
			xmlTemp = xmlTemp.substring (xmlTemp.indexOf ("</layout>") + "</layout>".length());
    		if (debug) Log.d (_TAG, "Tela: " + extracted);
    	}
    }
    
    /**
     * Método privado que recupera o xml gravado na tabela hash, de acordo com o índice apontado pelo
     * xml vindo do HTTP.
     */
    private void initLayout()
    {
    	String xmlToView = screens.get (index);
    	if (debug) Log.d(_TAG, "XML selecionado pelo HTTP: " + xmlToView);
    	while (xmlToView.indexOf ("<area") != -1)
    	{
    		String strColor = xmlToView.substring (xmlToView.indexOf ("background=\"") + "background=\"".length(), xmlToView.indexOf ("\" rect"));
    		String strDimensions = xmlToView.substring (xmlToView.indexOf ("rect=\"") + "rect=\"".length(), xmlToView.indexOf ("\" main"));
    		if (debug) Log.d (_TAG, "Cor: " + strColor);
    		if (debug) Log.d (_TAG, "Dimensões: " + strDimensions);
    		strDimensions = strDimensions.replaceAll ("%,", "@");
    		strDimensions = strDimensions.replaceAll ("%", "@");
    		if (debug) Log.d (_TAG, "Dimensões transformadas: " + strDimensions);
    		String[] dimensions = strDimensions.split("@");
    		for (int i = 0; i < dimensions.length; i++)
    		{
    			int num = Integer.parseInt (dimensions[i]);
    			if (debug) Log.d (_TAG, "Porcentagem: " + dimensions[i]);
    			if (debug) Log.d (_TAG, "num antes: " + num);
    			num = (i%2 == 0 ? (num * point.x) / 100 : (num * point.y) / 100);
    			if (debug) Log.d (_TAG, "num depois: " + num);
    			dimensions[i] = num + ""; 
    		}
    		xmlToView = xmlToView.substring (xmlToView.indexOf ("</area>") + "</area>".length());
    		createWebView (dimensions[0], dimensions[1], dimensions[2], dimensions[3], strColor);
    	}
    }
    
    /**
     * Método privado para criar o WebView.
     * @param oX coordenada inicial X
     * @param oY coordenada inicial Y
     * @param dX coordenada destino X
     * @param dY coordenada destino Y
     * @param color cor em hexadecimal (sistema RGB)
     */
    private void createWebView (String oX, String oY, String dX, String dY, String color)
    {
    	String textToHtml = "(" + oX + "," + oY + ") " + dX + "x" + dY;
    	WebView webView = new WebView (this);
    	webView.setTranslationX (Float.parseFloat (oX));
    	webView.setTranslationY (Float.parseFloat (oY));
    	webView.loadData ("<body bgcolor = " + color + " </body><html> <font color=\"#FFFFFF\"> " +  textToHtml + " </font></html>", "text/html", "UTF-8");
    	mainFrame.addView (webView, Integer.parseInt (dX), Integer.parseInt (dY));
    }
    
    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event) 
    {
        if ((keyCode == KeyEvent.KEYCODE_HOME)) 
        {
            if (debug) Log.d (_TAG, "KEYCODE_HOME");
            showDialog ("'HOME'");
            return true;
        }
        if ((keyCode == KeyEvent.KEYCODE_BACK)) 
        {
        	if (debug) Log.d (_TAG, "KEYCODE_BACK");
            showDialog ("'BACK'");
            return true;
        }
        if ((keyCode == KeyEvent.KEYCODE_MENU)) 
        {
        	if (debug) Log.d (_TAG, "KEYCODE_MENU");
            showDialog("'MENU'");
            return true;
        }
        return false;
    }
     
    private void showDialog (String the_key)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder (this);
        String message = "";
        if (the_key != null)
        {
        	message = "Você pressionou a tecla " + the_key + ". ";
        }
        message += "Deseja sair do 4YouSee?";
        builder.setMessage (message)
              .setCancelable (true)
               .setPositiveButton ("Sim", new DialogInterface.OnClickListener() 
               {
                   public void onClick (DialogInterface dialog, int id) 
                   {
                       dialog.cancel();
                       finish();
                   }
               })
               .setNegativeButton ("Não", new DialogInterface.OnClickListener() 
               {
                   public void onClick(DialogInterface dialog, int id) 
                   {
                        dialog.cancel();
                   }
               });
        AlertDialog alert = builder.create();
        alert.setTitle ("4YouSee");
        alert.show();
    }
}
