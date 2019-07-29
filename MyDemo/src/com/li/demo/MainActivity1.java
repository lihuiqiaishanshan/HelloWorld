package com.li.demo;

import java.io.DataOutputStream;
import java.io.OutputStream;

import iapp.eric.utils.base.Trace;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity1 extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
//		execAsRoot("pm disable com.konka.kkmetrowidget.ifengweatherwidget4metro");
//		execAsRoot("pm disable com.konka.ifengweatherwidget");
		setContentView(R.layout.activity_nhua_midware);
		
//		Intent i = new Intent("com.konka.multimedia.PLAY_CONTROL");
//		i.putExtra("com.konka.multimedia.PLAY_CONTROL.COMMAND", "com.konka.multimedia.PLAY_CONTROL.NEXT");
//		sendBroadcast(i);
		
		
		
		
	}
	
	
	
	
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		Trace.Info("###lhq keycode"+keyCode);
		if(keyCode == KeyEvent.KEYCODE_ENTER){
			Toast.makeText(getApplicationContext(), "收到OK键", Toast.LENGTH_SHORT).show();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	
	
	
	






	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		Trace.Info("###lfhq" + event.getKeyCode());
		return super.dispatchKeyEvent(event);
	}






	private void execAsRoot(String cmd){
		
		Trace.Info("###lhq cmd:"+cmd);
		java.lang.Process p1;
		try {
			p1 = Runtime.getRuntime().exec(cmd);
			OutputStream os = p1.getOutputStream();
			DataOutputStream dos = new DataOutputStream(os);
			//dos.writeBytes(cmd+"\n");
			dos.writeBytes("exit\n");
			dos.flush();
			int i = p1.waitFor();
			Trace.Info("###lhq"+i);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	

//    private static String TAG = MainActivity.class.getSimpleName();
//    private ActivityManager am = null;
//    private PackageManager pm = null;
//
//    private static Signature konkaMediaSig = new Signature("308203cf308202b7a00302010202090082c301eedea4fe28300d06092a864886f70d0101050500307f310b300906035504061302434e3112301006035504080c094775616e67446f6e673111300f06035504070c085368656e5a68656e310e300c060355040a0c054b4f4e4b41310b3009060355040b0c025456310c300a06035504030c03524443311e301c06092a864886f70d010901160f5456524443406b6f6e6b612e636f6d301e170d3132303333313132353733385a170d3339303831373132353733385a307f310b300906035504061302434e3112301006035504080c094775616e67446f6e673111300f06035504070c085368656e5a68656e310e300c060355040a0c054b4f4e4b41310b3009060355040b0c025456310c300a06035504030c03524443311e301c06092a864886f70d010901160f5456524443406b6f6e6b612e636f6d30820120300d06092a864886f70d01010105000382010d00308201080282010100cfc7a1a86aae5c4eff3772b3f5b312663795606a23789068b10da56b437f519c420be984200fc965cdc74482f9a07ef43d78f43459553652d30aea146b465891a36af1d9c6dfa7dbda089aeb7b12d5bab8aa1c6e0af9de108645bc7122e09023be1984edfe70f83d2e161dababb4331e1a5547a3a96080f3442e10fd1a9c163623d40efe71ea448804e4fa6af11b55855a7f729f30fae3dd6f9761e9a7c57e394dd01c4844e2920ab4b66f1c0f446e0d78e76a91f36d76b68b8fb3e6c0749abd8a3dc2ce4766da0ad9790e45d4fd80dd647b1d4d009d0fc122323c0e7cdb3c24105f4fad2aea03ea5a70a9d82d66b43cf0330d4514709b22bb7dfb277b1eeb37020103a350304e301d0603551d0e0416041469f96e8e5323afed61c1d1cafaf99f8b282b6d1a301f0603551d2304183016801469f96e8e5323afed61c1d1cafaf99f8b282b6d1a300c0603551d13040530030101ff300d06092a864886f70d010105050003820101006ddc3bc8f88883e3162f4fac57b0a1ee4dc020e0fbeb7dee068ddfb6ff2e777ca7abf8ce579bed1ddd43860fc854447d486fe034323c496b6c599f26977511ae60525ee17c03236148112eb42f496b51be623f99fe2122b1d1b4dff1bffe5dcab8f6ed71aa7a1961ad3f42f080bed8c4b49c5417993d934f4d037e291d7b9ad421c9ad819b6dc40350211709b3c5ba83bc31e2802ec56c78feb51f4c7cd1f08cbcba29cac95e1f7d6f054cafc3b11e144acd599711150eea3437b8d505502f4bca796806d72757e64325226648ed7fc5ef4bf348b767d13b1ac51bd65774add403ca1ad15513bfad7d9cdff5d0b27ec4c4500f1b71bd6ca5c024796f9f5ca798");
//    private static Signature konkaReleaseSig = new Signature("308203cf308202b7a003020102020900d5afd00a73da50c2300d06092a864886f70d0101050500307f310b300906035504061302434e3112301006035504080c094775616e67446f6e673111300f06035504070c085368656e5a68656e310e300c060355040a0c054b4f4e4b41310b3009060355040b0c025456310c300a06035504030c03524443311e301c06092a864886f70d010901160f5456524443406b6f6e6b612e636f6d301e170d3132303333313132353731385a170d3339303831373132353731385a307f310b300906035504061302434e3112301006035504080c094775616e67446f6e673111300f06035504070c085368656e5a68656e310e300c060355040a0c054b4f4e4b41310b3009060355040b0c025456310c300a06035504030c03524443311e301c06092a864886f70d010901160f5456524443406b6f6e6b612e636f6d30820120300d06092a864886f70d01010105000382010d00308201080282010100eb81ea4ef7c2e159e6e660e7a59856ce66968d63523495453d14fe29c9c06fac51926681beda44f91d4a3db65a91b25116c54851ab1c1bde47459b0bc6032274282b0e71dee914857b0f52b033155be1d461b79a4079d423944ce25e12db2169c37207bf7141958027719e71e4b44331b83950bf6d0bd57688d7b2d44d20c19fa66845ee53aa37f349698425d88cabfda5e7217512095db3b5f33deef1a697b8b0523be0c78917ea63e497adb728ac50761617a54b62c22d76af0d432fe42d8a48cfdb2ea08f406272b93166268c6cf4a628cf8ecfb0b3148d76354efa745dfa57b8906930c447245170946f7416cfa27eadaf78d5b19d8cd162b3fe6db275af020103a350304e301d0603551d0e041604143fc299725c410fe8d468883cd9c1a9e0eafdfa35301f0603551d230418301680143fc299725c410fe8d468883cd9c1a9e0eafdfa35300c0603551d13040530030101ff300d06092a864886f70d0101050500038201010024632393953dc5f332a636c880ad1e8d64be70c974b88ff79c636210635b388d080cbaefe78e3f673848652cc3439454361f5b4932bcfe308508c664f866dda37c8a36cc36e20ad1f5d20d9e2daab93eae251d408e91d6298a9df1a35389c2fbf1e91726409a59ddf3b1ff70e8aac629bbd00a34026191d6a806668079a849b6fdf29013068e69929e0416e36a09367cfb9e569cd29c01f2a8ee8f894098606e5dc7724c0ad47a1e0b04fcd77fadfd92c6b024d82ae4d9d203ae68292f2d1bb86eb0579a2e6c3440e24c94570fe66ceadcedb9717d51b19d7ca67c5b70ec5557cdae12b0cf5f8ab27f629012743e641f91c7aaf3924633fddf9067fa1df93295");
//    private static Signature konkaSharedSig = new Signature("308203cf308202b7a0030201020209009d3187045dabf61e300d06092a864886f70d0101050500307f310b300906035504061302434e3112301006035504080c094775616e67446f6e673111300f06035504070c085368656e5a68656e310e300c060355040a0c054b4f4e4b41310b3009060355040b0c025456310c300a06035504030c03524443311e301c06092a864886f70d010901160f5456524443406b6f6e6b612e636f6d301e170d3132303333313132353935375a170d3339303831373132353935375a307f310b300906035504061302434e3112301006035504080c094775616e67446f6e673111300f06035504070c085368656e5a68656e310e300c060355040a0c054b4f4e4b41310b3009060355040b0c025456310c300a06035504030c03524443311e301c06092a864886f70d010901160f5456524443406b6f6e6b612e636f6d30820120300d06092a864886f70d01010105000382010d00308201080282010100c469e0243401055789f529b3c51aba2dd582d29a867195293eff49417b9dc4a9fa0f0aef9611d1c457149a14c8e9a543e24e9117c9b715d8669977f44a21b991800b51207899133b9433f741f48f8095889a535b4dafa46305dbd72aae71c68e0e71616c86254af87aa41ce9a09b1bae3d7af8879760accb36c28d1f5adbfc2ea31df40dd244085431e3bd2fd8bc771a7b6422feafb2e5f7df691e5e2e5dfdbc8979d66d1794762ce59f70c9bdf98df447d69640755e620bd472daf70767df78609b49ca437c378af11561c34efedd0a96b3ef91655929adb5c66d307e9553a57d3a1be372051550c00beb154f76caac4a4dd85fac763051aa86b3a95ff8355f020103a350304e301d0603551d0e041604145bc6bbff5bd544e3f63f4c5b25234f2b9960e6cd301f0603551d230418301680145bc6bbff5bd544e3f63f4c5b25234f2b9960e6cd300c0603551d13040530030101ff300d06092a864886f70d01010505000382010100468179b6f4ad32f724b3819e86f139f50d82dcc0d5b1f3a1c97f739785604787ce4778a3d680f1a073c5816a9d11ed4f8c4c2ca2d50ef2da15d993652f64608588bc4cd023cb9339f400464f94bf9c4bcf461820c7c9f45998ffbd741aa5184c9e73c6d595fd1ebf1e2b6317051f7b1988c6d8ab209272317362b5333aae8204b79b2c979a30e73b70f9f75cdc4c0c887139837896293c636dc5ea91e8804cb94f0db2f31ccd11114894aa5da8701cfbdc9be9243bcded8d0acb20815e3ff8f9a2a8eb9c20dc835d13772947831f6e71d06d30780ce038cf21f20c7268302065a03abffbc83cd0f1cd504887b4365df424503026e7c50ea74e785f600b70ef3f");
//    private static Signature konkaPlatformSig = new Signature("308203cf308202b7a003020102020900bebb2b4102ee53e0300d06092a864886f70d0101050500307f310b300906035504061302434e3112301006035504080c094775616e67446f6e673111300f06035504070c085368656e5a68656e310e300c060355040a0c054b4f4e4b41310b3009060355040b0c025456310c300a06035504030c03524443311e301c06092a864886f70d010901160f5456524443406b6f6e6b612e636f6d301e170d3132303333313133303030385a170d3339303831373133303030385a307f310b300906035504061302434e3112301006035504080c094775616e67446f6e673111300f06035504070c085368656e5a68656e310e300c060355040a0c054b4f4e4b41310b3009060355040b0c025456310c300a06035504030c03524443311e301c06092a864886f70d010901160f5456524443406b6f6e6b612e636f6d30820120300d06092a864886f70d01010105000382010d00308201080282010100d976a6d2a515e7a42818e23674259809751fc2283c3007291385c7f4cd117af27106af57cdcfefb21c4db883a70f20c709afb18154316b0f9b95e10ce66df9def766d06946b74cd5e9f5cc8b3d04a08cc665a9ab4b963d917dbaac07a3d91c39109bb322b65d4a7e7c6d059325fb92954f53a2081c63e9f5e69af375d110442d55c4112a3fb4166e17ff814a6fdf26ea82015a4c12d98c56b6fadc846cd1ca94cb37afd443dd029956564a4db3938ea2067de7fd3443487f17342f5eab3bea52aa6c7e34f5840fdc48ca5d2ca76c5f92b2e9bc12c6840fb6690fc48431520d7d4da27326eabf71fe710a6d65a5123dc62451e5eb3dabf99861950d9977b88323020103a350304e301d0603551d0e0416041436c499ccb63a0ee244bf4b6df85be0a198c2c693301f0603551d2304183016801436c499ccb63a0ee244bf4b6df85be0a198c2c693300c0603551d13040530030101ff300d06092a864886f70d010105050003820101007beeb1d4992ce7a87ab9130866f1838be54d134fc95d23d0696f10bb9b454a96c7820b5a4343c78eaf9bd53ca4a0be04a05ccdfedc38f7a44e68cd57f3cad669d8aaecfb53a90a6080b075d9fbea1a7f3428cada6ea79cd88ecd7f8e834bb479cc0399c9f7374f9523795d41cf347f158bdf3a4eb892b081a348b723712dbef24a33c058f541c9276d5ccc12b98d016691f56bf035206ebc9b5c31fcaab4557c16d28302bad86833f97ef97a889b709df3f8f91663e05f68cd41ec79873005508a432d21ebbf1fb4841e5f38e752b5b08c6c91ca29b607cdb1717c5740b6bfbd0a258d56c7761b5d39ddb6c004912755a251c45029c96204f5be72f71f3342f4");
//
//    
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        Trace.setTag("lhq");
//        setContentView(R.layout.activity_test);
//        Trace.Info("##############################################");
//        Button b =  (Button) findViewById(R.id.contentProvider);
//        b.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				Trace.Info("###hehe" + Process.myUid());
//				Trace.Info("111111111111111111111111111111111111111111111111111111111111" + pm.getClass().getName());
//				Log.d(TAG, "is konka signature : " + IsKonkaSignature(Process.myUid()));
//			}
//		});
//        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        pm = getPackageManager();
//
//        
////        Log.d(TAG, "is konka signature : " + IsKonkaSignature(Process.myUid()));
//        Trace.Info("222222222222222222222222222222222222222222222222222");
//    }
//       
//    // 根据uid来判断程序是否使用康佳签名(注意: 根据Uid获取对应的签名时,其过程中获取的包名可能不是调用者的包名. 但是uid相同,签名一定相同. 所以不影响结果,另外对于apk来说,只能有一个签名,所以获取签名数组的第一项就够了)
//    private boolean IsKonkaSignature(int uid) {
//        String[] packages = pm.getPackagesForUid(uid);
//        String packageName = packages.length > 0 ? packages[0] : null;
//        
//        Signature sig = null;
////            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
//        	PackageInfo packageInfo = null;
//			try {
//				packageInfo = AppGlobals.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES, Process.myUid());
//			} catch (RemoteException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				return false;
//			}
//			Trace.Info("###lhq" + (packageInfo != null));
//			Trace.Info("###lhq" + (packageInfo.signatures != null));
//			Trace.Info("###lhq" + (packageInfo.signatures.length));
//            if(packageInfo != null && packageInfo.signatures != null && packageInfo.signatures.length > 0) {
//                sig = packageInfo.signatures[0];
//            } else {
//                return false;
//            }
//        
//        return sig.equals(konkaMediaSig) || sig.equals(konkaPlatformSig) || sig.equals(konkaReleaseSig) || sig.equals(konkaSharedSig);
//    }
}
