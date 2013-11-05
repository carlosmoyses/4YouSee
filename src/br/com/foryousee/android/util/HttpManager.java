package br.com.foryousee.android.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpManager 
{
	private static HttpManager manager = null;
	private OutputStream out;
	private InputStream in;
	
	public static HttpManager getInstance()
	{
		if (manager == null)
			manager = new HttpManager();
		return manager;
	}
	
	/**
	 * Método para enviar dados via http recebendo uma String como resposta.
	 * @param strUrl URL de onde se deseja enviar os dados.
	 * @param dados dados a serem enviados
	 * @return String resposta
	 * @throws IOException
	 */
	public String send (String strUrl, String dados) throws IOException 
	{
	    // Create a new HttpClient and Post Header
	    String result = null;
	    URL url;
    	if (strUrl.indexOf ("http://") == -1)
    	{
    		strUrl = "http://" + strUrl.trim();
    	}
    	url = new URL (strUrl);
    	HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    	urlConnection.setDoOutput(true);
    	urlConnection.setChunkedStreamingMode(0);

    	out = new BufferedOutputStream (urlConnection.getOutputStream());
    	out.write (dados.getBytes());
    	out.flush();
    	out.close();
    	in = new BufferedInputStream (urlConnection.getInputStream());
    	result = convertStreamToString (in);
    	in.close();
	    return result;
	}
	private String convertStreamToString (InputStream is) 
	{
	    /*
	     * To convert the InputStream to String we use the BufferedReader.readLine()
	     * method. We iterate until the BufferedReader return null which means
	     * there's no more data to read. Each line will appended to a StringBuilder
	     * and returned as String.
	     */
		BufferedReader reader = new BufferedReader (new InputStreamReader (is));
	    StringBuilder sb = new StringBuilder();

	    String line = null;
	    try 
	    {
	    	while ((line = reader.readLine()) != null) 
	    	{
	            sb.append(line + "\n");
	        }
	    } 
	    catch (IOException e) 
	    {
	        e.printStackTrace();
	    } 
	    finally 
	    {
	        try 
	        {
	            is.close();
	        } 
	        catch (IOException e) 
	        {
	            e.printStackTrace();
	        }
	    }
	    return sb.toString();
	}
}
