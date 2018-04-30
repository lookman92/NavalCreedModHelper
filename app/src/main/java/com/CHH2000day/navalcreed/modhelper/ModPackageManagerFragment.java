package com.CHH2000day.navalcreed.modhelper;
import android.support.v4.app.*;
import android.view.*;
import android.support.v7.widget.*;
import android.widget.*;
import android.os.*;
import android.content.res.*;
import android.widget.CompoundButton.*;
import android.support.v7.app.*;
import android.content.*;
import java.util.*;
import android.support.design.widget.*;

public class ModPackageManagerFragment extends Fragment implements ModPackageManager.OnDataChangedListener
{

	private View v;
	private RecyclerView recyclerview;
	private MyAdapter adapter;
	private ToggleButton ovrd_switch;


	public static final int COLOR_ECAM_AMBER=0xD97900;
	public static final int COLOR_ECAM_GREEN=0x00E300;
	@Override
	public View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		// TODO: Implement this method
		super.onCreateView ( inflater, container, savedInstanceState );
		v = inflater.inflate ( R.layout.modmanagerfragmemt, null );
		recyclerview = (RecyclerView)v.findViewById ( R.id.modmanagerfragmemtRecyclerView );
		ovrd_switch = (ToggleButton)v.findViewById ( R.id.modmanagerswitcherToggleButton1 );
		return v;
	}

	@Override
	public void onActivityCreated ( Bundle savedInstanceState )
	{
		// TODO: Implement this method
		super.onActivityCreated ( savedInstanceState );
		if ( ModPackageManager.getInstance ( ).isOverride ( ) )
		{
			ovrd_switch.setChecked ( true );
		}
		adapter = new MyAdapter ( getActivity ( ) );
		ovrd_switch.setOnCheckedChangeListener ( new OnCheckedChangeListener ( ){

				@Override
				public void onCheckedChanged ( CompoundButton p1, boolean isChecked )
				{
					if ( !isChecked )
					{
						if ( ModPackageManager.getInstance ( ).isOverride ( ) )
						{
							ModPackageManager.getInstance ( ).setIsOverride ( false );
						}
						return;
					}
					AlertDialog.Builder adb= new AlertDialog.Builder ( getActivity ( ) );
					adb.setTitle ( R.string.notice )
						.setMessage ( "超控mod管理机制将使mod管理器失效，是否继续？" )
						.setNegativeButton ( R.string.cancel, new DialogInterface.OnClickListener ( ){

							@Override
							public void onClick ( DialogInterface p1, int p2 )
							{
								// TODO: Implement this method
								cancel ( );
							}
						} )
						.setPositiveButton ( R.string.cont, new DialogInterface.OnClickListener ( ){

							@Override
							public void onClick ( DialogInterface p1, int p2 )
							{
								override ( );
								// TODO: Implement this method
							}
						} )
						.setCancelable ( false );
					AlertDialog ad=adb.create ( );
					ad.setCanceledOnTouchOutside ( false );
					ad.show ( );
					// TODO: Implement this method
				}
				private void override ( )
				{
					ModPackageManager.getInstance ( ).setIsOverride ( true );

				}
				private void cancel ( )
				{
					ovrd_switch.setChecked ( false );
					if ( ModPackageManager.getInstance ( ).isOverride ( ) )
					{
						ModPackageManager.getInstance ( ).setIsOverride ( false );
					}
				}
			} );

		if ( !ModPackageManager.getInstance ( ).isOverride ( ) )
		{
			recyclerview.setLayoutManager ( new LinearLayoutManager ( getActivity ( ), LinearLayoutManager.VERTICAL, false ) );
			recyclerview.setAdapter ( adapter );
			ModPackageManager.getInstance ( ).setonDataChangedListener ( this );
		}
	}

	@Override
	public void onResume ( )
	{
		// TODO: Implement this method
		super.onResume ( );

		onChange ( );
	}

	@Override
	public void onPause ( )
	{
		// TODO: Implement this method
		super.onPause ( );

	}

	@Override
	public void onDestroyView ( )
	{
		// TODO: Implement this method
		super.onDestroyView ( );
		ModPackageManager.getInstance ( ).unregistDataChangeListener ( );
	}


	@Override
	public void onChange ( )
	{	if ( adapter != null && recyclerview != null )
		{
			adapter = null;
			adapter = new MyAdapter ( getActivity ( ) );	
			recyclerview.setAdapter ( adapter );
		}
		// TODO: Implement this method
	}


	private class MyAdapter extends RecyclerView.Adapter
	{

		private Context context;
		private LayoutInflater li;
		private String[] keys;
		private OnLongClickListener listener;
		public MyAdapter ( Context ctx )
		{
			context = ctx;
			li = LayoutInflater.from ( context );
			keys = ModPackageManager.PUBLIC_KEYS;
			listener = new UninstallListener ( );
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder ( ViewGroup p1, int p2 )
		{
			View vi=li.inflate ( R.layout.modmanager_item, null );
			// TODO: Implement this method
			return new ViewHolder ( vi );
		}

		@Override
		public void onBindViewHolder ( RecyclerView.ViewHolder p1, int p2 )
		{View v=( (ViewHolder)p1 ).getView ( );
			RelativeLayout rl=(RelativeLayout)v.findViewById ( R.id.modmanageritemRelativeLayout );
			TextView info=(TextView)v.findViewById ( R.id.modmanageritemTextView );
			TextView memo=(TextView)v.findViewById ( R.id.modmanageritemMemo );
			rl.setTag ( p2 );
			String key=keys [ p2 ];
			String type="";
			String subtype="";
			if ( key.equals ( ModPackageInfo.SUB_MODTYPE_CV_CN ) || key.equals ( ModPackageInfo.SUB_MODTYPE_CV_EN ) || key.equals ( ModPackageInfo.SUB_MODTYPE_CV_JP_BB ) || key.equals ( ModPackageInfo.SUB_MODTYPE_CV_JP_CV ) || key.equals ( ModPackageInfo.SUB_MODTYPE_CV_JP_CA ) || key.equals ( ModPackageInfo.SUB_MODTYPE_CV_JP_DD ) )
			{
				type = ModPackageInfo.MODTYPE_CV;
				subtype = key;
			}
			else
			{
				type = key;
				subtype = ModPackageInfo.SUBTYPE_EMPTY;
			}
			//如果对应mod包已安装
			if ( ModPackageManager.getInstance ( ).checkInstalled ( type, subtype ) )
			{
				info.setText ( new StringBuilder ( ).append ( getString(R.string.modtype) )
							  .append ( ModPackageManager.getInstance().resolveModType ( keys [ p2 ] ) )
							  .append ( "\n" )
							  .append ( ModPackageManager.getInstance ( ).getModName ( keys [ p2 ] ) ) );
				rl.setOnLongClickListener ( listener );
				memo.setText(R.string.long_click_to_uninstall);

			}
			else
			{
				memo.setText ( "" );
				info.setText ( new StringBuilder ( ).append ( getString(R.string.modtype))
							  .append ( ModPackageManager.getInstance().resolveModType ( keys [ p2 ] ) )
							  .append ( "\n" )
							  .append ( "mod未安装" ).toString ( ) );
			}


			// TODO: Implement this method
		}

		@Override
		public int getItemCount ( )
		{
			if ( ModPackageManager.getInstance ( ).isOverride ( ) )
			{
				return 0;
			}
			// TODO: Implement this method
			return keys.length;
		}




	}
	private class UninstallListener implements OnLongClickListener
	{

		@Override
		public boolean onLongClick ( View p1 )
		{
			if ( ModPackageManager.getInstance ( ).isOverride ( ) )
			{
				//OVRD时禁用管理器
				return false;
			}
			int num=p1.getTag ( );
			String modtype=ModPackageManager.PUBLIC_KEYS [ num ];
			if ( modtype.equals ( ModPackageInfo.MODTYPE_OTHER ) )
			{
				Snackbar.make ( v, "This category of mod package can't be uninstalled", Snackbar.LENGTH_LONG ).show ( );
			}
			if ( modtype.equals ( ModPackageInfo.SUB_MODTYPE_CV_CN ) || modtype.equals ( ModPackageInfo.SUB_MODTYPE_CV_EN ) )
			{
				if ( ModPackageManager.getInstance ( ).checkInstalled ( ModPackageInfo.MODTYPE_CV, modtype ) )
				{
					uninstall ( modtype );
				}
			}
			else
			{
				if ( ModPackageManager.getInstance ( ).checkInstalled ( modtype, ModPackageInfo.SUBTYPE_EMPTY ) )
				{
					uninstall ( modtype );
				}
			}


			// TODO: Implement this method
			return true;
		}
		private void uninstall ( final String key )
		{
			AlertDialog.Builder adb=new AlertDialog.Builder ( getActivity ( ) );
			adb.setTitle ( R.string.notice )
				.setMessage ( getString(R.string.confirm_to_remove_changes_to_parta) + ModPackageManager.getInstance().resolveModType ( key ) + ":" + ModPackageManager.getInstance ( ).getModList ( ).get ( key ) + getString(R.string.confirm_to_remove_changes_to_partb) )
				.setNegativeButton ( R.string.cancel, null )
				.setPositiveButton ( R.string.cont, new DialogInterface.OnClickListener ( ){

					@Override
					public void onClick ( DialogInterface p1, int p2 )
					{
						String type=	key.equals ( ModPackageInfo.SUB_MODTYPE_CV_CN ) || key.equals ( ModPackageInfo.SUB_MODTYPE_CV_EN ) ?ModPackageInfo.MODTYPE_CV: key;
						String subType=type.equals ( ModPackageInfo.MODTYPE_CV ) ?key: ModPackageInfo.SUBTYPE_EMPTY;
						boolean b=ModPackageManager.getInstance ( ).requestUninstall ( type, subType , (ModHelperApplication)getActivity ( ).getApplication ( ) );
						String str=b ?getString(R.string.success): getString(R.string.failed);
						Snackbar.make ( v, str, Snackbar.LENGTH_LONG ).show ( );
						// TODO: Implement this method
					}
				} );
			adb.create ( ).show ( );

		}

	}
	private class ViewHolder extends RecyclerView.ViewHolder
	{

		private View v;
		public ViewHolder ( View v )
		{
			super ( v );
			this.v = v;
		}
		public View getView ( )
		{
			return v;
		}
	}
}
