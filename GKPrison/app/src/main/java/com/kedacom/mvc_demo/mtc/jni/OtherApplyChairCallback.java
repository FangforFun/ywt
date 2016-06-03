package com.kedacom.mvc_demo.mtc.jni;

import com.gkzxhn.gkprison.application.AppStackManager;
import com.kedacom.mvc_demo.vconf.bean.MtId;
import com.kedacom.mvc_demo.vconf.bean.MtInfo;
import com.kedacom.mvc_demo.vconf.modle.OtherApplyDialogManager;
import com.kedacom.mvc_demo.vconf.modle.VideoConferenceService;
import com.kedacom.truetouch.mtc.BaseCallbackHandler;
import com.pc.utils.StringUtils;

/**
 * 他人申请成为管理方
 * @author ryf
 * @date 2013-8-30
 * 
 *       <pre>
 * ------------------------------
 * <?xml version="1.0" encoding="utf-8"?>
 * <TrueTouchAndroid>
 * 	<EventID>
 * 		29347
 * 	</EventID>
 * 	<Message>
 * 		<OtherApplyChair>
 * 			<Result>1</Result>
 * 			<McuNo>192</McuNo>
 * 			<TerNo>1</TerNo>
 * 		</OtherApplyChair>
 * 	</Message>
 * </TrueTouchAndroid>
 * ------------------------------
 * </pre>
 */
public class OtherApplyChairCallback extends BaseCallbackHandler {

	@Override
	public void addCallback(String strXML) {
		android.util.Log.i("vconf", "OtherApplyChair");

		loadParseSingleXML(strXML);
		boolean result = result4SingleXML();

		if (!result) {
			return;
		}

		final String mcuNo = getSingleObject("McuNo");
		final String terNo = getSingleObject("TerNo");
		String alias = VideoConferenceService.getAliasByTerNo(terNo);
		if (StringUtils.isNull(alias)) {
			alias = terNo;
		}
		MtInfo info = new MtInfo(alias, new MtId(mcuNo, terNo));
		OtherApplyDialogManager.showOtherApplyChairDialog(AppStackManager.Instance().currentActivity(), info);

	}
}
