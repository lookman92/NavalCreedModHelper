package com.CHH2000day.navalcreed.modhelper;
import java.util.*;
import java.io.*;
import okio.*;
import org.json.*;

public class ModPackageManager
{
	//<ModType,ModName>
	private HashMap<String,String> installedMod;
	private File configFile;
	private boolean isOverride=false;
	private static final String OVRD="override";
	private static ModPackageManager mmm;
	private OnDataChangedListener OnDataChangedListener;
	private static final String[] CATEORY_BG={"loading","loadingmap","matching"};
	public static final String[] PUBLIC_KEYS={ModPackageInfo.MODTYPE_BACKGROUND,ModPackageInfo.MODTYPE_BGM,ModPackageInfo.MODTYPE_SOUNDEFFECT,ModPackageInfo.MODTYPE_SOUNDEFFECT_PRIM,ModPackageInfo.MODTYPE_SOUNDEFFECT_SEC,ModPackageInfo.MODTYPE_CREWPIC,ModPackageInfo.SUB_MODTYPE_CV_CN,ModPackageInfo.SUB_MODTYPE_CV_EN};
	private String[] used_prim_keys;
	private  String[] used_sec_keys;

	static{

	}
	public void setonDataChangedListener ( OnDataChangedListener odcl )
	{
		OnDataChangedListener = odcl;
	}
	public void unregistDataChangeListener ( )
	{
		OnDataChangedListener = null;
	}

	public synchronized static ModPackageManager getInstance ( )
	{
		if ( mmm == null )
		{
			mmm = new ModPackageManager ( );
		}
		return mmm;
	}
	private ModPackageManager ( )
	{}
	public void init ( File storedFile ) throws  IOException, JSONException
	{
		used_prim_keys = Arrays.copyOf ( PUBLIC_KEYS, PUBLIC_KEYS.length - 2 );
		used_sec_keys = Arrays.copyOfRange ( PUBLIC_KEYS, PUBLIC_KEYS.length - 2, PUBLIC_KEYS.length );
		configFile = storedFile;
		installedMod = new HashMap<String,String> ( );
		reflesh ( );

	}
	private void reflesh ( ) throws JSONException, IOException
	{
		try
		{
			Source s=Okio.source ( configFile );
			BufferedSource bs=Okio.buffer ( s );
			JSONObject jo=new JSONObject ( bs.readUtf8 ( ) );
			bs.close ( );
			/*installedMod.put ( ModPackageInfo.MODTYPE_BACKGROUND, jo.getString ( ModPackageInfo.MODTYPE_BACKGROUND ) );
			 installedMod.put ( ModPackageInfo.MODTYPE_BGM, jo.getString ( ModPackageInfo.MODTYPE_BGM ) );
			 installedMod.put ( ModPackageInfo.MODTYPE_CREWPIC, jo.getString ( ModPackageInfo.MODTYPE_CREWPIC ) );
			 installedMod.put ( ModPackageInfo.MODTYPE_SOUNDEFFECT, jo.getString ( ModPackageInfo.MODTYPE_SOUNDEFFECT ) );
			 JSONObject j=jo.getJSONObject ( ModPackageInfo.MODTYPE_CV );
			 installedMod.put ( ModPackageInfo.SUB_MODTYPE_CV_CN, j.getString ( ModPackageInfo.SUB_MODTYPE_CV_CN ) );
			 installedMod.put ( ModPackageInfo.SUB_MODTYPE_CV_EN, j.getString ( ModPackageInfo.SUB_MODTYPE_CV_EN ) );
			 */
			for ( String type:used_prim_keys )
			{
				installedMod.put ( type, getStringFromJsonObject ( jo, type ) );

			}
			JSONObject jcv=jo.getJSONObject ( ModPackageInfo.MODTYPE_CV );
			for ( String subc:used_sec_keys )
			{
				installedMod.put ( subc, getStringFromJsonObject ( jcv, subc ) );
			}
			if ( jo.has ( OVRD ) )
			{
				isOverride = jo.getBoolean ( OVRD );
			}
			if ( OnDataChangedListener != null )
			{
				OnDataChangedListener.onChange ( );
			}
		}
		catch (FileNotFoundException t)
		{try
			{
				updateConfig ( true );
				reflesh ( );
			}
			catch (FileNotFoundException e)
			{}
			catch (JSONException e)
			{}
		}

	}
	private String getValue ( String key )
	{
		if ( installedMod.containsKey ( key ) )
		{
			return installedMod.get ( key );
		}
		return "";
	}
	private String getStringFromJsonObject ( JSONObject jo, String key )
	{
		if ( jo.has ( key ) )
		{
			try
			{
				return jo.getString ( key );
			}
			catch (JSONException e)
			{}
		}
		return "";
	}
	private void commit ( ) throws IOException, JSONException
	{
		updateConfig ( false );
		reflesh ( );
	}
	public boolean requestUninstall ( String modtype, String subtype, ModHelperApplication app )
	{
		boolean b=performUninstall ( modtype, subtype, app );
		postUninstall ( modtype, subtype );

		return b;
	}

	public boolean performUninstall ( String modtype, String subtype, ModHelperApplication app )
	{
		int subt=ModPackageInstallHelper.SUBTYPE_NULL;
		if ( subtype.equals ( ModPackageInfo.SUB_MODTYPE_CV_CN ) )
		{
			subt = ModPackageInstallHelper.SUBTYPE_CV_CN;
		}
		if ( subtype.equals ( ModPackageInfo.SUB_MODTYPE_CV_EN ) )
		{
			subt = ModPackageInstallHelper.SUBTYPE_CV_EN;
		}
		String path=ModPackageInstallHelper.getPath ( modtype, subt, app );
		if ( path.equals ( "" ) )
		{
			return false;
		}
		//背景替换与船员头像替换部分路径重叠，故而需要防止误删
		if ( modtype.equals ( ModPackageInfo.MODTYPE_BACKGROUND ) )
		{
			for ( String s:CATEORY_BG )
			{
				if ( !Utils.delDir ( new File ( path, s ) ) )
				{
					return false;
				}
			}
			return true;
		}
		//Category Other can't be uninstalled due to path definition
		if ( modtype.equals ( ModPackageInfo.MODTYPE_OTHER ) )
		{
			return false;
		}
		else
		{
			return Utils.delDir ( new File ( path ) );
		}

	}

	public void postUninstall ( String modtype, String subtype )
	{
		if ( isOverride )
		{
			return;
		}
		if ( modtype.equals ( ModPackageInfo.MODTYPE_CV ) )
		{
			installedMod.put ( subtype, "" );
		}
		else
		{
			installedMod.put ( modtype, "" );
		}
		try
		{
			commit ( );
		}
		catch (IOException e)
		{e.printStackTrace ( );}
		catch (JSONException e)
		{e.printStackTrace ( );}
		return;

	}
	public void postInstall ( String modtype, String subtype, String modname )
	{
		if ( isOverride )
		{
			return;
		}
		if ( modtype.equals ( ModPackageInfo.MODTYPE_CV ) )
		{
			installedMod.put ( subtype, modname );
		}
		else
		{
			installedMod.put ( modtype, modname );
		}
		try
		{
			commit ( );
		}
		catch (IOException e)
		{e.printStackTrace ( );}
		catch (JSONException e)
		{e.printStackTrace ( );}

	}

	private void updateConfig ( boolean isNew ) throws JSONException, IOException
	{
		JSONObject jo=new JSONObject ( );
		if ( isNew || installedMod.isEmpty ( ) )
		{
			/*
			 jo.put ( ModPackageInfo.MODTYPE_BGM, "" );
			 jo.put ( ModPackageInfo.MODTYPE_BACKGROUND, "" );
			 jo.put ( ModPackageInfo.MODTYPE_CREWPIC, "" );
			 jo.put ( ModPackageInfo.MODTYPE_SOUNDEFFECT, "" );

			 JSONObject jcv=new JSONObject ( );
			 jcv.put ( ModPackageInfo.SUB_MODTYPE_CV_CN, "" );
			 jcv.put ( ModPackageInfo.SUB_MODTYPE_CV_EN, "" );
			 jo.put ( ModPackageInfo.MODTYPE_CV, jcv );
			 writeConfigFile ( jo );*/
			for ( String type:used_prim_keys )
			{
				if ( type.equals ( ModPackageInfo.MODTYPE_CV ) )
				{
					JSONObject jcv=new JSONObject ( );
					for ( String subc:used_sec_keys )
					{
						jcv.put ( subc, "" );
					}
					jo.put ( type, jcv );
				}
				else
				{
					jo.put ( type, "" );
				}
			}
			writeConfigFile ( jo );
			return;			
		}
		else
		{
			/*
			 jo.put ( ModPackageInfo.MODTYPE_BGM, getValue ( ModPackageInfo.MODTYPE_BGM ) );
			 jo.put ( ModPackageInfo.MODTYPE_BACKGROUND, getValue ( ModPackageInfo.MODTYPE_BACKGROUND ) );
			 jo.put ( ModPackageInfo.MODTYPE_CREWPIC, getValue ( ModPackageInfo.MODTYPE_CREWPIC ) );
			 jo.put ( ModPackageInfo.MODTYPE_SOUNDEFFECT, getValue ( ModPackageInfo.MODTYPE_SOUNDEFFECT ) );
			 JSONObject jcv=new JSONObject ( );
			 jcv.put ( ModPackageInfo.SUB_MODTYPE_CV_CN, getValue ( ModPackageInfo.SUB_MODTYPE_CV_CN ) );
			 jcv.put ( ModPackageInfo.SUB_MODTYPE_CV_EN, getValue ( ModPackageInfo.SUB_MODTYPE_CV_EN ) );
			 jo.put ( ModPackageInfo.MODTYPE_CV, jcv );
			 jo.put ( OVRD, isOverride );
			 writeConfigFile ( jo );
			 */
			for ( String type:used_prim_keys )
			{
				if ( type.equals ( ModPackageInfo.MODTYPE_CV ) )
				{
					JSONObject jcv=new JSONObject ( );
					for ( String subc:used_sec_keys )
					{
						jcv.put ( subc, getValue ( subc ) );
					}
					jo.put ( type, jcv );
					continue;
				}
				else
				{
					jo.put ( type, getValue ( type ) );
				}
			}
			jo.put ( OVRD, isOverride );
			writeConfigFile ( jo );
			reflesh();
		}


	}
	private void writeConfigFile ( JSONObject jo ) throws  IOException
	{
		if ( !configFile.getParentFile ( ).exists ( ) )
		{
			configFile.getParentFile ( ).mkdirs ( );
		}
		if ( configFile.isDirectory ( ) )
		{
			Utils.delDir ( configFile );
		}
		Sink s=Okio.sink ( configFile );
		BufferedSink bs=Okio.buffer ( s );
		bs.writeUtf8 ( jo.toString ( ) );
		bs.flush ( );
		bs.close ( );
	}
	public void setIsOverride ( boolean isOverride )
	{
		this.isOverride = isOverride;
		try
		{
			commit ( );
		}
		catch (IOException e)
		{e.printStackTrace ( );}
		catch (JSONException e)
		{e.printStackTrace ( );}
	}

	public boolean isOverride ( )
	{
		return isOverride;
	}

	public HashMap<String,String> getModList ( )
	{
		return installedMod;
	}
	public String getModName ( String datatype )
	{
		String s=getModList ( ).get ( datatype );
		if ( s == null )
		{
			return "";
		}
		return s;
	}

	public boolean checkInstalled ( String type, String subtype )
	{
		if ( isOverride )
		{
			return false;
		}
		if ( type.equals ( ModPackageInfo.MODTYPE_CV ) )
		{
			return ( !"".equals ( getValue ( subtype ) ) );
		}
		return ( !"".equals ( getValue ( type ) ) );
	}

	public static String resolveModType ( String modtype )
	{
		String s="";
		if ( ModPackageInfo.MODTYPE_BACKGROUND.equals ( modtype ) )
		{
			s = "背景图片";
		}
		else if ( ModPackageInfo.MODTYPE_BGM.equals ( modtype ) )
		{
			s = "背景音乐";
		}
		else if ( ModPackageInfo.MODTYPE_CREWPIC.equals ( modtype ) )
		{
			s = "船员头像";
		}
		else if ( ModPackageInfo.MODTYPE_CV.equals ( modtype ) )
		{
			s = "舰长语音";
		}
		else if ( ModPackageInfo.SUB_MODTYPE_CV_CN.equals ( modtype ) )
		{
			s = "舰长语音-中文";
		}
		else if ( ModPackageInfo.SUB_MODTYPE_CV_EN.equals ( modtype ) )
		{
			s = "舰长语音-英文";
		}
		else if ( ModPackageInfo.MODTYPE_SOUNDEFFECT.equals ( modtype ) )
		{
			s = "音效(已弃用)";
		}
		else if ( ModPackageInfo.MODTYPE_SOUNDEFFECT_PRIM.equals ( modtype ) )
		{
			s = "主音效";
		}
		else if ( ModPackageInfo.MODTYPE_SOUNDEFFECT_SEC.equals ( modtype ) )
		{
			s = "副音效";
		}
		else
		{
			s = "未知";
		}
		return s;
	}
	public static interface OnDataChangedListener
	{
		public void onChange ( );
	}
}
