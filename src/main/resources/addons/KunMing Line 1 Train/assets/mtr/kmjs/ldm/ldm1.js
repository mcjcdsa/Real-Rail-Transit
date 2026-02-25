importPackage(java.awt);
importPackage(java.awt.geom);
importPackage(java.awt.image);
include(Resources.id("mtr:kmjs/wh_display_helper.js"));
include(Resources.id("mtr:kmjs/wh_util.js"));
include(Resources.id("mtr:kmjs/mtr_util.js"));//去除重复常量声明的版本
//字体声明在 wh_const.js 中，部分函数声明在 wh_util.js 中

//LDM贴图大小及分辨倍率
const LDM_LT_WIDTH = 1364;//灯光贴图宽
const LDM_LT_HEIGHT = 186;//灯光贴图高
const LDM_AF = 4;//分辨率倍率，线路图部分需要较高分辨率，在此倍增
const LDM_WIDTH = LDM_LT_WIDTH * LDM_AF;//LDM宽
const LDM_HEIGHT = LDM_LT_HEIGHT * LDM_AF;//LDM高
include("draw_ldm_km1.js");

//LDM模型创建(obj贴图替换法)
/*
let raw_LDM_BG_Model = ModelManager.loadRawModel(Resources.manager(), Resources.idr("ldm_dl12_bg.obj"), null);//右侧闪灯图位置
raw_LDM_BG_Model.applyUVMirror(false, true);
raw_LDM_BG_Model.setAllRenderType("light");
var LDM_BG_Model = ModelManager.uploadVertArrays(raw_LDM_BG_Model);

let raw_LDM_MAP_Model = ModelManager.loadRawModel(Resources.manager(), Resources.idr("ldm_dl12_map.obj"), null);
raw_LDM_MAP_Model.applyUVMirror(false, true);
raw_LDM_MAP_Model.setAllRenderType("interior");
var LDM_MAP_Model = ModelManager.uploadVertArrays(raw_LDM_MAP_Model);
*/

//LDM模型创建(四点定位)
const LDM_LR1_RightPos = [[-0.7581, 2.2833, -1.9481], [-0.8403, 2.1148, -1.9481], [-0.8403, 2.1148, -3.3231], [-0.7581, 2.2833, -3.3231]];
const LDM_LR1_LeftPos = [[0.7580, 2.2833, -3.3231], [0.8402, 2.1148, -3.3231], [0.8402, 2.1148, -1.9481], [0.7580, 2.2833, -1.9481]];
const LDM_LR2_RightPos = getOffsetPos(LDM_LR1_RightPos, -0.0002).Pos;
const LDM_LR2_LeftPos = getOffsetPos(LDM_LR1_LeftPos, 0.0002).Pos;

const LDM_IN_slotCfg = {//interior部分，外图层
    "version": 1,
    "texSize": [LDM_WIDTH, 2 * LDM_HEIGHT],//贴图大小，左右LDM都画在同一张贴图上，所以高是2倍
    "slots": [
        {
            "name": "ldm_right",
            "texArea": [0, 0, LDM_WIDTH, LDM_HEIGHT],//该部分在贴图中的位置
            "pos": [
                LDM_LR2_RightPos
            ],
            "offsets": [[0, 0, -5], [0, 0, 0], [0, 0, 5], [0, 0, 10]]
        },
        {
            "name": "ldm_left",
            "texArea": [0, LDM_HEIGHT, LDM_WIDTH, LDM_HEIGHT],
            "pos": [
                LDM_LR2_LeftPos
            ],
            "offsets": [[0, 0, -5], [0, 0, 0], [0, 0, 5], [0, 0, 10]]
        }
    ]
};
const LDM_LT_slotCfg = {//light部分，内图层
    "version": 1,
    "texSize": [LDM_LT_WIDTH, 2 * LDM_LT_HEIGHT],//贴图大小，左右LDM都画在同一张贴图上，所以高是2倍
    "slots": [
        {
            "name": "ldm_right",
            "texArea": [0, 0, LDM_LT_WIDTH, LDM_LT_HEIGHT],//该部分在贴图中的位置
            "pos": [
                LDM_LR1_RightPos
            ],
            "offsets": [[0, 0, -5], [0, 0, 0], [0, 0, 5], [0, 0, 10]]
        },
        {
            "name": "ldm_left",
            "texArea": [0, LDM_LT_HEIGHT, LDM_LT_WIDTH, LDM_LT_HEIGHT],
            "pos": [
                LDM_LR1_LeftPos
            ],
            "offsets": [[0, 0, -5], [0, 0, 0], [0, 0, 5], [0, 0, 10]]
        }
    ]
};

var LDM_IN_dhBase = new DisplayHelperRE(LDM_IN_slotCfg, "interior", [255, 255, 255, 255]);//渲染阶段为"interior"，全亮度
var LDM_LT_dhBase = new DisplayHelperRE(LDM_LT_slotCfg, "light", [255, 255, 255, 255]);//渲染阶段为"light"，全亮度

//LDM相关参数
const LDM_FONT_CJK = SOURCE_HAN_SANS_CN_BOLD;//中文用字体
const LDM_FONT_NONCJK = SANS_LAO;//英文用字体
const LDM_RateLimit = 0.15;          //km      //LDM 整体帧率限制，默认10fps
const LDM_Flash_RateLimit = 0.3;    //km       //LDM 下一站站点提示闪烁时间，默认3.33fps
const LDM_FlashNextStn_RateLimit = 0.15;//km   //LDM 下一站站点提示闪烁时间，默认3fps
const LDM_ARV_showNextRoute = false;           //LDM 到站提示 到达同台换向（比如站前折返）终点站时，是否提前绘制下一线路首发站到站信息，否则绘制本线路终点站到站信息
const LDM_MAP_INFO = {                         //LDM 线路图绘制参数
    backgroundColor: rgbToColor(254, 254, 254),//LDM 线路图背景颜色
    LTBackgroundColor: rgbToColor(30, 30, 30), //LDM 灯光背景颜色
    DoorArrowOffColor: rgbToColor(8, 18, 8), //LDM 门状态箭头颜色-关闭
    DoorArrowOnColor: rgbToColor(66, 153, 66), //LDM 门状态箭头颜色-开
    defaultLEDNumber: 0,                       //LDM 默认小灯珠数量
    shouldLimitLEDNumber: true,                //LDM 是否限制小灯珠数量，是则恒定，否则根据大灯珠间隔增减
    shouldDrawStnNameARW: true,                //LDM 是否绘制站名小箭头
    shouldDrawIntChangeARW: true,              //LDM 是否绘制换乘箭头环

    lightColor_NextStnMain: Color.GREEN, //km  //LDM 后续站大灯珠颜色
    shouldFlashNextFirstStn: true,      //km   //LDM 下一站灯珠是否闪烁
    lightColor_NextFirstStnMain: Color.YELLOW, //LDM 下一站大灯珠颜色
    lightColor_NextStnLit: Color.RED,          //LDM 后续站小灯珠颜色
    lightColor_ThisStn: Color.RED,    //km     //LDM 当前站灯珠颜色
    lightColor_LastStn: Color.GREEN,           //LDM 上一站（已经离开正在前往下一站）灯珠颜色
    shouldDrawNextStnLLT: false,               //LDM 是否绘制后续站点的小灯珠
    shouldFlashThisStn: false,                 //LDM 本站灯珠是否闪烁

    lightColor_DoorOpn: Color.GREEN,           //LDM 开门灯珠颜色
    lightColor_DoorCls: Color.RED              //LDM 关门灯珠颜色
}
const LDM_TRANSFER = "换乘|Transfer to";//换乘线路前缀
const LDM_DOORTIPS_OPN = "本侧开门|Door Open at This Side";//本侧开门
//const LDM_DOORTIPS_OPN = "请从本侧车门下车|Please exit from this side";//本侧开门
const LDM_DOORTIPS_CLS = "请从对面车门下车|Please exit from the other side";//对侧开门

function create(ctx, state, train) {
    //创建动态贴图
    /*
    state.ldm_bg_Left_Texture = new GraphicsTexture(LDM_LT_WIDTH, LDM_LT_HEIGHT);
    state.ldm_bg_Right_Texture = new GraphicsTexture(LDM_LT_WIDTH, LDM_LT_HEIGHT);
    state.ldm_bg_left_Model = LDM_BG_Model.copyForMaterialChanges();
    state.ldm_bg_right_Model = LDM_BG_Model.copyForMaterialChanges();
    state.ldm_bg_left_Model.replaceTexture("ldm_dl12_bg.png", state.ldm_bg_Left_Texture.identifier);
    state.ldm_bg_right_Model.replaceTexture("ldm_dl12_bg.png", state.ldm_bg_Right_Texture.identifier);

    state.ldm_map_Left_Texture = new GraphicsTexture(LDM_WIDTH, LDM_HEIGHT);
    state.ldm_map_Right_Texture = new GraphicsTexture(LDM_WIDTH, LDM_HEIGHT);
    state.ldm_map_left_Model = LDM_MAP_Model.copyForMaterialChanges();
    state.ldm_map_right_Model = LDM_MAP_Model.copyForMaterialChanges();
    state.ldm_map_left_Model.replaceTexture("ldm_dl12_map.png", state.ldm_map_Left_Texture.identifier);
    state.ldm_map_right_Model.replaceTexture("ldm_dl12_map.png", state.ldm_map_Right_Texture.identifier);
    */
    state.LDM_IN_dh = LDM_IN_dhBase.create();
    state.LDM_LT_dh = LDM_LT_dhBase.create();

    //帧率限制设定(以下不建议直接更改)
    state.LDM_RateLimit = new RateLimit(LDM_RateLimit);
    state.LDM_Flash_RateLimit = new RateLimit(LDM_Flash_RateLimit);
    state.LDM_FlashNextStn_RateLimit = new RateLimit(LDM_FlashNextStn_RateLimit);//km

    //绘图设定及初始化
    state.repaintLDM = "begin";
    state.directionToDraw = train.isReversed();
    state.lastNextStationIndex = null;
    state.lastTrainStatus = null;
    state.lastRouteInfo = null;
    state.LDM_Info = {};
    state.displayState = {};

    state.doorOpnShouldRepaint = true;
    state.displayState.isLeftDoorOpn = false;
    state.displayState.isRightDoorOpn = false;
    state.displayState.shouldDrawDoorInfo = false;
    state.displayState.doorIsOpening = false;

    state.LDM_DoorState = new StateTracker();
    state.LDM_DoorState.setState("off");

    //输出渲染耗时，调试用
    state.LDMRenderTime = {//调试用，平均渲染耗时
        RenderTimes: 0, // 当前渲染次数
        totalRenderTime: 0, // 累计渲染时间
        maxRenderTime: 0 //最大渲染时间
    };
}

function dispose(ctx, state, train) {//列车消失时调用,清除列车状态并释放内存
    /*
    state.ldm_bg_Left_Texture.close();
    state.ldm_bg_Right_Texture.close();
    state.ldm_map_Left_Texture.close();
    state.ldm_map_Right_Texture.close();
    */
    state.LDM_IN_dh.close();
    state.LDM_LT_dh.close();

    state.repaintLDM = null;
    state.doorOpnShouldRepaint = null;
    state.lastNextStationIndex = null;
    state.lastTrainStatus = null;
    state.lastRouteInfo = null;
    state.LDM_Info = null;
    state.displayState = null;
}

function render(ctx, state, train) {//列车渲染每一帧调用
    let renderStartTime = Date.now();//记录开始时间，测试用

    //从jef的上海LDM那抄来的更新线路信息
    if (train.shouldRender() && train.shouldRenderDetail() && shouldRepaintLDM(state, train)) {
        let platformInfo;
        switch (state.trainStatus) {
            case STATUS_NO_ROUTE:
                platformInfo = null;
                break;
            case STATUS_RETURNING_TO_DEPOT:
                platformInfo = train.getAllPlatforms().get(train.getAllPlatforms().size() - 1);
                break;
            case STATUS_WAITING_FOR_DEPARTURE:
                platformInfo = train.getAllPlatforms().get(0);
                break;
            case STATUS_CHANGING_ROUTE:
                platformInfo = train.getAllPlatforms().get(train.getAllPlatformsNextIndex());
                break;
            default:
                platformInfo = train.getThisRoutePlatforms().get(train.getThisRoutePlatformsNextIndex());
                break;
        }
        let stationInfoList = [];
        if (checkNextRoute(train, state.trainStatus, LDM_ARV_showNextRoute)) {
            platformInfo = train.getAllPlatforms().get(train.getAllPlatformsNextIndex() + 1);
            stationInfoList = getStationInfoListNear3(train, state.trainStatus, train.getAllPlatformsNextIndex());
        }
        const routeInfo = getRouteInfo(train, state.trainStatus, platformInfo);
        if (stationInfoList.length > 0) {
            routeInfo.stationInfoList = stationInfoList;
        }
        if (!checkJsonProperty(state, "routeInfo", routeInfo)) { // 如果 state 中不存在 routeInfo 或 routeInfo 改变，则更新 routeInfo
            print("Train " + train.id() + " LDM_routeInfo：" + JSON.stringify(routeInfo));
            state.routeInfo = routeInfo;
        }
    }
    if (state.routeInfo != state.lastRouteInfo && state.trainStatus != STATUS_RETURNING_TO_DEPOT) {//更新检定
        state.shouldRepaintLDM = true;
    }

    //绘制LDM
    if (train.shouldRender() && train.shouldRenderDetail() && state.LDM_RateLimit.shouldUpdate()) {
        let nextStationIndex = state.trainStatus == STATUS_RETURNING_TO_DEPOT ? 1 : train.getThisRoutePlatformsNextIndex();
        nextStationIndex = checkNextRoute(train, state.trainStatus, LDM_ARV_showNextRoute) ? 0 : nextStationIndex;

        if (state.trainStatus == STATUS_NO_ROUTE || state.routeInfo == null) {// 无线路，绘制提示
            if (state.trainStatus != state.lastTrainStatus) {
                let g = state.LDM_IN_dh.graphicsFor("ldm_left");
                drawBlueScreen(g, "无线路信息 什么都不会显示的啦", LDM_MAP_INFO.backgroundColor, Color.BLACK);
                g = state.LDM_IN_dh.graphicsFor("ldm_right");
                drawBlueScreen(g, "无线路信息 什么都不会显示的啦", LDM_MAP_INFO.backgroundColor, Color.BLACK);
                state.LDM_IN_dh.upload();

                g = state.LDM_LT_dh.graphicsFor("ldm_left");
                drawLDMLTBG(g, LDM_LT_WIDTH, LDM_LT_HEIGHT, LDM_MAP_INFO.LTBackgroundColor);
                g = state.LDM_LT_dh.graphicsFor("ldm_right");
                drawLDMLTBG(g, LDM_LT_WIDTH, LDM_LT_HEIGHT, LDM_MAP_INFO.LTBackgroundColor);
                state.LDM_LT_dh.upload();
            }
        } else if (state.trainStatus == STATUS_RETURNING_TO_DEPOT) {//回库车，熄灯
            if (state.trainStatus != state.lastTrainStatus) {
                let g = state.LDM_LT_dh.graphicsFor("ldm_left");
                drawLDMLTBG(g, LDM_LT_WIDTH, LDM_LT_HEIGHT, LDM_MAP_INFO.LTBackgroundColor);
                g = state.LDM_LT_dh.graphicsFor("ldm_right");
                drawLDMLTBG(g, LDM_LT_WIDTH, LDM_LT_HEIGHT, LDM_MAP_INFO.LTBackgroundColor);
                state.LDM_LT_dh.upload();
            }
        } else {//正常运行
            //-------------------------------------------静--态--部--分--绘--制-------------------------------------------
            let nextRoutePlatformInfo = getNextRoute(train, state.trainStatus) == null ? null : train.getAllPlatforms().get(train.getAllPlatforms().indexOf(train.getThisRoutePlatforms().get(train.getThisRoutePlatforms().size() - 1)) + 1);
            let nextRouteFirstStation = nextRoutePlatformInfo == null ? null : nextRoutePlatformInfo.station;
            let thisRouteDestination = train.getThisRoutePlatforms().get(train.getThisRoutePlatforms().size() - 1).station;

            if ((state.trainStatus == STATUS_ARRIVED && state.lastTrainStatus != STATUS_ARRIVED) && ((nextStationIndex == state.routeInfo.stationInfoList.length - 1 && nextRouteFirstStation && thisRouteDestination == nextRouteFirstStation) || state.routeInfo.stationInfoList[nextStationIndex].reverseAtPlatform)) {//已到达终点站并且下一站是本站或站台折返，则翻转绘制方向
                state.directionToDraw = !state.directionToDraw;
            }

            if (state.shouldRepaintLDM) {//若 routeInfo 改变则更新线路图静态内容
                //列出换乘信息。没有换乘:0,单线换乘:1,多线换乘:2,只要类型、名称、颜色，统计2类数量
                state.listStationInterchange = listStationInterchange_dl(state.routeInfo.stationInfoList, state.routeInfo.stationInfoList.length);
                //print("listStationInterchange:" + JSON.stringify(state.listStationInterchange, null, 2));

                if (state.trainStatus == STATUS_WAITING_FOR_DEPARTURE || state.trainStatus == STATUS_LEAVING_DEPOT) {//列车未出库，重置绘制方向
                    state.directionToDraw = false;
                }

                let g = state.LDM_IN_dh.graphicsFor("ldm_right");
                let LDM_Info = drawLDMInterior(g, state.routeInfo.routeColor, state.routeInfo.stationInfoList, state.directionToDraw, LDM_MAP_INFO, state.listStationInterchange);
                g = state.LDM_IN_dh.graphicsFor("ldm_left");
                drawLDMInterior(g, state.routeInfo.routeColor, state.routeInfo.stationInfoList, !state.directionToDraw, LDM_MAP_INFO, state.listStationInterchange);
                state.LDM_IN_dh.upload();

                state.directionToDrawNow = state.directionToDraw;
                state.LDM_Info = {
                    AC_int: LDM_Info.AC_int / LDM_AF,
                    LC_n: LDM_Info.LC_n,
                    LC_int: LDM_Info.LC_int / LDM_AF,
                    AF: LDM_Info.AF,
                    door_xs1: LDM_Info.door_xs1 / LDM_AF,
                    door_xs2: LDM_Info.door_xs2 / LDM_AF,
                    door_ys: LDM_Info.door_ys / LDM_AF
                }
                print("LDM_Info:" + JSON.stringify(state.LDM_Info, null, 2));
                //print("destination:" + thisRouteDestination + ", firstStation:" + nextRouteFirstStation);
                state.shouldRepaintLDM = false;
            }

            // nextStationIndex 改变或 trainStatus 变为 STATUS_ARRIVED 则更新灯光板
            if ((nextStationIndex != state.lastNextStationIndex || state.lastRouteInfo != state.routeInfo || (state.trainStatus == STATUS_ARRIVED && state.lastTrainStatus != STATUS_ARRIVED)) && state.routeInfo.stationInfoList.length > 0 && state.routeInfo.stationInfoList[nextStationIndex]) {
                let g = state.LDM_LT_dh.graphicsFor("ldm_right");
                drawLDMLTSP(g, LDM_MAP_INFO, state.LDM_Info, state.directionToDrawNow, nextStationIndex, state.routeInfo.stationInfoList.length);
                g = state.LDM_LT_dh.graphicsFor("ldm_left");
                drawLDMLTSP(g, LDM_MAP_INFO, state.LDM_Info, !state.directionToDrawNow, nextStationIndex, state.routeInfo.stationInfoList.length);

                state.displayState.lightCont = -1;

                //print("nextStationIndex:" + nextStationIndex);
                state.lastNextStationIndex = nextStationIndex;
            }

            // 列车开门时 或 刷新列车时已经开门 则更新开关门信息
            let DoorOpeningState = train.isDoorOpening();
            if ((DoorOpeningState && !state.doorOpnState) || (state.doorOpnShouldRepaint && (train.doorValue() == 1 || train.doorValue() > state.lastDoorValue))) {
                //判断两侧车门是否打开
                let doorOpnInfo = checkDoorhasOpn(train);
                if (doorOpnInfo.isLeftDoorOpn || doorOpnInfo.isRightDoorOpn) {
                    state.displayState.isLeftDoorOpn = doorOpnInfo.isLeftDoorOpn;
                    state.displayState.isRightDoorOpn = doorOpnInfo.isRightDoorOpn;
                    state.displayState.shouldDrawDoorInfo = true;
                    state.LDM_DoorState.setState("opening");
                    state.doorOpnShouldRepaint = false;
                } else if (train.doorValue() > 0) {//如果两侧车门均不能开启，但确实开门了，重复检测
                    state.doorOpnShouldRepaint = true;
                } else {
                    state.doorOpnShouldRepaint = false;
                }
                print("Train " + train.id() + " Door is opening, info: isLeftDoorOpn:" + doorOpnInfo.isLeftDoorOpn + ", isRightDoorOpn:" + doorOpnInfo.isRightDoorOpn);
            }
            //如果车门正在关闭 则更新开关门信息
            if ((state.lastDoorValue == 1 && train.doorValue() < 1) || (state.doorOpnShouldRepaint && train.doorValue() < state.lastDoorValue)) {
                let doorOpnInfo = checkDoorhasOpn(train);
                if (doorOpnInfo.isLeftDoorOpn || doorOpnInfo.isRightDoorOpn) {
                    state.displayState.isLeftDoorOpn = doorOpnInfo.isLeftDoorOpn;
                    state.displayState.isRightDoorOpn = doorOpnInfo.isRightDoorOpn;
                    state.displayState.shouldDrawDoorInfo = true;
                    state.LDM_DoorState.setState("closing");
                    state.doorOpnShouldRepaint = false;
                } else if (train.doorValue() > 0) {
                    state.doorOpnShouldRepaint = true;
                } else {
                    state.doorOpnShouldRepaint = false;
                }
                print("Train " + train.id() + " Door is closing, info: isLeftDoorOpn:" + doorOpnInfo.isLeftDoorOpn + ", isRightDoorOpn:" + doorOpnInfo.isRightDoorOpn);
            }
            if (train.doorValue() == 0 && train.doorValue() < state.lastDoorValue) {//关门后，熄灯
                state.displayState.shouldDrawDoorInfo = true;
                state.LDM_DoorState.setState("off");
            }
            state.doorOpnState = DoorOpeningState;
            state.lastDoorValue = train.doorValue();
            if (state.lastTrainStatus == STATUS_ARRIVED && state.trainStatus != STATUS_ARRIVED) {//出站后重置开关门信息
                state.displayState.isLeftDoorOpn = false;
                state.displayState.isRightDoorOpn = false;
                state.displayState.shouldDrawDoorInfo = false;
                state.displayState.doorIsOpening = false;
            }

            //-------------------------------------------动--态--部--分--绘--制-------------------------------------------

            //列车到站则绘制本站闪烁
            if (state.trainStatus == STATUS_ARRIVED) {
                let g = state.LDM_LT_dh.graphicsFor("ldm_right");
                drawLDMLTTS(g, LDM_MAP_INFO, state.LDM_Info, state.directionToDrawNow, nextStationIndex, state.routeInfo.stationInfoList.length, state.LDM_Flash_RateLimit, state.LDM_FlashNextStn_RateLimit, state.displayState);//km
                g = state.LDM_LT_dh.graphicsFor("ldm_left");
                drawLDMLTTS(g, LDM_MAP_INFO, state.LDM_Info, !state.directionToDrawNow, nextStationIndex, state.routeInfo.stationInfoList.length, state.LDM_Flash_RateLimit, state.LDM_FlashNextStn_RateLimit, state.displayState);//km
                if (state.displayState.shouldDrawDoorInfo) {
                    g = state.LDM_LT_dh.graphicsFor("ldm_right");
                    drawLDMLTDoorInfo(g, LDM_MAP_INFO, state.LDM_Info, state.displayState, true, state.LDM_DoorState);
                    g = state.LDM_LT_dh.graphicsFor("ldm_left");
                    drawLDMLTDoorInfo(g, LDM_MAP_INFO, state.LDM_Info, state.displayState, false, state.LDM_DoorState);
                    state.displayState.shouldDrawDoorInfo = false;
                }
            } else {//其他时候绘制行进状态（上一站闪烁）
                let g = state.LDM_LT_dh.graphicsFor("ldm_right");
                drawLDMLTNS(g, LDM_MAP_INFO, state.LDM_Info, state.directionToDrawNow, nextStationIndex, state.routeInfo.stationInfoList.length, state.LDM_Flash_RateLimit, state.LDM_FlashNextStn_RateLimit, state.displayState);//km
                g = state.LDM_LT_dh.graphicsFor("ldm_left");
                drawLDMLTNS(g, LDM_MAP_INFO, state.LDM_Info, !state.directionToDrawNow, nextStationIndex, state.routeInfo.stationInfoList.length, state.LDM_Flash_RateLimit, state.LDM_FlashNextStn_RateLimit, state.displayState);//km
            }
            state.LDM_LT_dh.upload();

            state.lastTrainStatus = state.trainStatus;//保存本次列车状态
            state.lastRouteInfo = state.routeInfo;
        }
    }//LDM绘制结束

    //渲染模型部分
    for (let i = 0; i < train.trainCars(); i++) {
        if (i == 0) {//头车
            drawLDMModel(ctx, state.LDM_IN_dh.model, i, [0.0], null);
            drawLDMModel(ctx, state.LDM_LT_dh.model, i, [0.0], null);
        } else if (i == train.trainCars() - 1) {//尾车
            drawLDMModel(ctx, state.LDM_IN_dh.model, i, [0.2712], null);
            drawLDMModel(ctx, state.LDM_LT_dh.model, i, [0.2712], null);
        } else {//中间车
            drawLDMModel(ctx, state.LDM_IN_dh.model, i, [0.0], null);
            drawLDMModel(ctx, state.LDM_LT_dh.model, i, [0.0], null);
        }
    }

    let renderEndTime = Date.now();// 记录渲染结束时间
    LDMRenderTime(state, renderStartTime, renderEndTime);//输出渲染时间，调试用
}

function shouldRepaintLDM(state, train) {
    let trainStatus = getTrainStatus(train);
    if (checkProperty(state, "trainStatus", trainStatus)) { // 如果 state 中存在 trainStatus 且符合当前状态
        if (state.repaintLDM != "already") {
            state.repaintLDM = "already";
            return true;
        }
        return false;
    } else {
        state.trainStatus = trainStatus;
        print("trainStatus:" + trainStatus);
        return true;
    }
}

function LDMRenderTime(state, renderStartTime, renderEndTime) {
    let renderTime = renderEndTime - renderStartTime;// 计算渲染一帧的时间
    // 更新状态
    state.LDMRenderTime.RenderTimes += 1; // 渲染次数加 1
    state.LDMRenderTime.totalRenderTime += renderTime; // 累计渲染时间
    if (renderTime > state.LDMRenderTime.maxRenderTime) {// 最大渲染时间
        state.LDMRenderTime.maxRenderTime = renderTime;
    }
    // 检查是否达到 1000 次渲染，是则输出平均渲染耗时
    if (state.LDMRenderTime.RenderTimes >= 1000) {
        let averageRenderTime = state.LDMRenderTime.totalRenderTime / state.LDMRenderTime.RenderTimes; // 计算平均渲染时间
        averageRenderTime = averageRenderTime.toFixed(3);
        print("KM1LDM:{averageRenderTime:" + averageRenderTime + "ms,totalRenderTime:" + state.LDMRenderTime.totalRenderTime + "ms,maxRenderTime:" + state.LDMRenderTime.maxRenderTime + "ms}");
        state.LDMRenderTime.RenderTimes = 0;
        state.LDMRenderTime.totalRenderTime = 0;
        state.LDMRenderTime.maxRenderTime = 0;
    }
}

function getOffsetPos(pos, d) {//偏移图层坐标
    // 计算两个点之间的距离
    const dx = pos[1][0] - pos[0][0];
    const dy = pos[1][1] - pos[0][1];
    const h = Math.sqrt(dx * dx + dy * dy);
    if (h == 0) {// 处理特殊情况，避免除以零
        throw new Error("The distance between pos[0] and pos[1] is zero, cannot calculate direction.");
    }

    // 计算偏移量
    const ddx = dy * d / h;
    const ddy = dx * d / h;

    // 计算并返回新的坐标
    const newPos = pos.map(point => {
        return [point[0] + ddx, point[1] + ddy, point[2]];
    });

    return { Pos: newPos };
}

function drawLDMModel(ctx, model, i, zOffset, zOffsetF) {
    let matrices = new Matrices(); // 引入恢复矩阵
    let k = zOffset ? zOffset.length : 0; // 如果zOffset不为空，获取其长度
    if (zOffset != null && k > 0) {
        matrices.pushPose();
        for (let j = 0; j < k; j++) {
            matrices.translate(0, 0, zOffset[j]); // 平移
            ctx.drawCarModel(model, i, matrices); // 绘制车厢模型
            matrices.popPushPose();
        }
        matrices.popPose();
    }
    k = zOffsetF ? zOffsetF.length : 0; // 如果zOffsetF不为空，获取其长度
    if (zOffsetF != null && k > 0) {
        matrices.pushPose();
        for (let j = 0; j < k; j++) {
            matrices.translate(0, 0, zOffsetF[j]); // 平移
            matrices.rotateY(Math.PI); // 绕Y轴旋转180°
            ctx.drawCarModel(model, i, matrices); // 绘制车厢模型
            matrices.popPushPose();
        }
        matrices.popPose();
    }
}