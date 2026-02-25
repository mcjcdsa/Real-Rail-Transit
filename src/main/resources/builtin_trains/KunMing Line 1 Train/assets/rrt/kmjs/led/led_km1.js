importPackage(java.awt);
importPackage(java.awt.geom);
importPackage(java.awt.image);
include(Resources.id("rrt:kmjs/wh_display_helper.js"));
include(Resources.id("rrt:kmjs/wh_util.js"));
include(Resources.id("rrt:kmjs/rrt_util.js"));//去除重复常量声明的版本
//字体声明在 wh_const.js 中，部分函数声明在 wh_util.js 中

//LED贴图大小
const LED_in_PixelSize = 6;//                                     车内 LED 单个点阵大小, 单位为像素
const LED_in_PixelNumber = 16;//                                  车内 LED 字符的高度由几个点阵组成
const LED_in_Str_Height = LED_in_PixelSize * LED_in_PixelNumber;//车内 LED 文本部分高, 即 点阵大小 * 点阵数量
const LED_in_Str_WIDTH = LED_in_Str_Height * 5.5;//               车内 LED 文本部分宽, 武汉6号线所用车内LED比例为 8:1，所以此处设为 8
const LED_in_Str_Spacing = 1;//                                   车内 LED 点阵缝隙宽 1 像素
const LED_in_WIDTH = LED_in_Str_WIDTH + LED_in_Str_Spacing;//     车内 LED 总宽度，缝隙 1
const LED_in_HEIGHT = LED_in_Str_Height + LED_in_Str_Spacing;//   车内 LED 总高度，缝隙 1

const LED_HeadOut_Str_Height = LED_in_PixelSize * LED_in_PixelNumber;//  车头外 LED 文本部分高, 即 点阵大小 * 点阵数量
const LED_HeadOut_Str_WIDTH = LED_HeadOut_Str_Height * 7;//              车头外 LED 文本部分宽, 比例为 5.5:1，所以此处设为 5.5
const LED_HeadOut_WIDTH = LED_HeadOut_Str_WIDTH + LED_in_Str_Spacing;//  车头外 LED 总宽度，缝隙 1
const LED_HeadOut_HEIGHT = LED_HeadOut_Str_Height + LED_in_Str_Spacing;//车头外 LED 总高度，缝隙 1

//模型参数设置
const LED_in_Pos = [[0.3438, 2.3032, 8.9788], [0.3438, 2.1782, 8.9788], [-0.3438, 2.1782, 8.9788], [-0.3438, 2.3032, 8.9788]];
const LED_HeadOut_Pos = [[-0.4375, 2.0038, 10.4701], [-0.4375, 1.8788, 10.4701], [0.4375, 1.8788, 10.4701], [0.4375, 2.0038, 10.4701]];
//四点定位，四个位置分别对应左上、左下、右下、右上，此处模型 z 轴居中，后面再用矩阵移动到渲染处
const LED_in_slotCfg = {
    "version": 1,
    "texSize": [LED_in_WIDTH, LED_in_HEIGHT],//贴图大小
    "slots": [
        {
            "name": "led_display_in",
            "texArea": [0, 0, LED_in_WIDTH, LED_in_HEIGHT],//该部分在贴图中的位置
            "pos": [
                LED_in_Pos
            ],
            "offsets": [[0, 0, 0]]
        }
    ]
};
const LED_HeadOut_slotCfg = {
    "version": 1,
    "texSize": [LED_HeadOut_WIDTH, LED_HeadOut_HEIGHT],//贴图大小
    "slots": [
        {
            "name": "led_display_HeadOut",
            "texArea": [0, 0, LED_HeadOut_WIDTH, LED_HeadOut_HEIGHT],//该部分在贴图中的位置
            "pos": [
                LED_HeadOut_Pos
            ],
            "offsets": [[0, 0, 0]]
        }
    ]
};
var LED_in_dhBase = new DisplayHelperRE(LED_in_slotCfg, "light", [255, 255, 255, 255]);//渲染阶段为"light"，全亮度(纯白)
var LED_HeadOut_dhBase = new DisplayHelperRE(LED_HeadOut_slotCfg, "light", [255, 255, 255, 255]);

//LED相关参数
const LEDRollSpeed = 2 * LED_in_PixelSize;//LED 文本滚动速度，单位为像素/帧，必须是 PixelSize 的倍数
const LED_RateLimit = LEDRollSpeed / (3.1 * LED_in_Str_Height);//LED 整体帧率限制，每帧时间 = 滚动速度 / (每秒滚动字符数 * 字符大小)，建议在3左右
const LED_ARV_showNextRoute = true;//   LED 到达同台换向（比如站前折返）终点站时，是否提前绘制下一线路首发站到站信息，否则绘制本线路终点站到站信息
const LED_in_gridIsRound = true;//      LED 是否绘制圆形点阵，否则绘制方形点阵
const LED_in_Tips_NO_ROUTE = { Cjk: "无线路信息。", NonCjk: "No route." };
const LED_in_Tips_DEPOT = { Cjk: "本次列车为回库车，请勿乘坐。", NonCjk: "" };
const LED_HeadOut_Tips_NO_ROUTE = { Cjk: "无线路", NonCjk: "" };
const LED_HeadOut_Tips_DEPOT = { Cjk: "回库", NonCjk: "" };
const LED_Color = {
    BG_Color: rgbToColor(24, 24, 24),//未发光灯泡颜色
    Grid_Color: rgbToColor(16, 16, 16),//LED底色
    Text_In_Cjk_Color: Color.RED,//车内LED中文颜色
    Text_In_NonCjk_Color: Color.RED,//车内LED英文颜色
    Text_Out_Cjk_Color: rgbToColor(255, 214, 80),//车外LED中文颜色
    Text_Out_NonCjk_Color: rgbToColor(255, 214, 80)//车外LED英文颜色
};

//--------------------------------如-需-更-改-报-站-格-式-请-在-此-处-更-改---------------------------------------
//车内 LED 下一站信息
function getLEDNextStationStr(routeName, destination, stationName, interchange_CJK, interchange_NonCJK, RouteLength) {
    let routeNameCJK = TextUtil.getCjkParts(routeName);
    let routeNameNonCJK = TextUtil.getNonCjkParts(routeName);
    let destinationCJK = TextUtil.getCjkParts(destination);
    let destinationNonCJK = TextUtil.getNonCjkParts(destination);
    let StationNameCJK = TextUtil.getCjkParts(stationName);
    let StationNameNonCJK = TextUtil.getNonCjkParts(stationName);
    //RouteLength = (RouteLength / 1000).toFixed(2);
    return {
        //中文部分
        strCjk: "乘客们，本次列车终点站" + destinationCJK + "，下一站" + StationNameCJK + interchange_CJK + "。列车运行过程中，请站稳扶好，谨防跌倒",
        //英文部分
        strNonCjk: "Dear topassengers，this train terminal at " + destinationNonCJK + "\u00A0. the next station is " + StationNameNonCJK + "\u00A0" + interchange_NonCJK + ". Please keep your baggage and avoid forling when the train is moving.",
        //此处可以设置中英文文本空隙为几个字符
        gap: 4
    };
}

//车内 LED 下一站为终点站的信息
function getLEDToDestinationStr(routeName, destination, stationName, interchange_CJK, interchange_NonCJK, RouteLength) {
    let routeNameCJK = TextUtil.getCjkParts(routeName);
    let routeNameNonCJK = TextUtil.getNonCjkParts(routeName);
    let destinationCJK = TextUtil.getCjkParts(destination);
    let destinationNonCJK = TextUtil.getNonCjkParts(destination);
    let StationNameCJK = TextUtil.getCjkParts(stationName);
    let StationNameNonCJK = TextUtil.getNonCjkParts(stationName);
    //RouteLength = (RouteLength / 1000).toFixed(2);
    return {
        //中文部分
        strCjk: "乘客们，本次列车终点站" + destinationCJK + "，下一站" + StationNameCJK + interchange_CJK + "。列车运行过程中，请站稳扶好，谨防跌倒",
        //英文部分
        strNonCjk: "Dear topassengers，this train terminal at " + destinationNonCJK + "\u00A0. the next station is " + StationNameNonCJK + "\u00A0" + interchange_NonCJK + ". Please keep your baggage and avoid forling when the train is moving.",
        //此处可以设置中英文文本空隙为几个字符
        gap: 4
    };
}

//车内 LED 到站提示信息
function getLEDThisStationStr(stationName, interchange_CJK, interchange_NonCJK) {
    let StationNameCJK = TextUtil.getCjkParts(stationName);
    let StationNameNonCJK = TextUtil.getNonCjkParts(stationName);
    return {
        //中文部分
        strCjk: "乘客们，" + StationNameCJK + "到了" + interchange_CJK + "。请带好您的随身物品从开启的车门下车，开门请当心。",
        //英文部分
        strNonCjk: "We are now at " + StationNameNonCJK + "\u00A0" + interchange_NonCJK + ".",
        //此处可以设置中英文文本空隙为几个字符
        gap: 4
    };
}

//车内 LED 终点站到站提示信息
function getLEDArriveDestinationStr(stationName, interchange_CJK, interchange_NonCJK) {
    let StationNameCJK = TextUtil.getCjkParts(stationName);
    let StationNameNonCJK = TextUtil.getNonCjkParts(stationName);
    return {
        //中文部分
        strCjk: "乘客们，终点站" + StationNameCJK + "到了" + interchange_CJK + "。请带好您的随身物品从开启的车门下车，开门请当心。",
        //英文部分
        strNonCjk: "We are now at " + StationNameNonCJK + "\u00A0" + interchange_NonCJK + ".",
        //此处可以设置中英文文本空隙为几个字符
        gap: 4
    };
}

//----------------------------------------报-站-格-式-更-改-结-束--------------------------------------------
function create(ctx, state, train) {//列车出现时调用
    //创建模型和贴图
    state.LED_in_dh = LED_in_dhBase.create();
    state.LED_HeadOut_dh = LED_HeadOut_dhBase.create();

    //创建LED在各车厢的变换矩阵(如果end有多种形态，请在此创建后到下方渲染模型部分进一步调整)
    state.matrix_disply_F1 = createMatrix([[0, 0, 0], 0, 0, 0]);//头车前
    state.matrix_disply_F3 = createMatrix([[0, 0.0594, -1.1906], 0, 180, 0]);//头车后

    state.matrix_disply_R1 = createMatrix([[0, 0, 0], 0, 180, 0]);//尾车前
    state.matrix_disply_R3 = createMatrix([[0, 0.0594, 1.1906], 0, 0, 0]);//尾车后

    state.matrix_disply_M1 = createMatrix([[0, 0.0594, 0.9093], 0, 0, 0]);//中间车前
    state.matrix_disply_M2 = createMatrix([[0, 0.0594, -1.1906], 0, 180, 0]);//中间车后

    state.matrix_head_out_disply_F = createMatrix([[0, 0, 0], 0, 0, 0]);
    state.matrix_head_out_disply_R = createMatrix([[0, 0, 0], 0, 180, 0]);

    //----------------------------------------以-下-不-建-议-直-接-更-改---------------------------------------------
    //帧率限制设定、绘图设定及初始化
    state.LED_RateLimit = new RateLimit(LED_RateLimit);
    state.repaintLED = true;

    //绘制模拟 LED 点阵的网格到 state.ledGridTexture 上
    state.LED_WIDTH_Max = Math.max(LED_in_WIDTH, LED_HeadOut_WIDTH);
    state.LED_HEIGHT_Max = Math.max(LED_in_HEIGHT, LED_HeadOut_HEIGHT);
    state.ledGridTexture = new BufferedImage(state.LED_WIDTH_Max, state.LED_HEIGHT_Max, BufferedImage.TYPE_INT_ARGB);
    let gs = state.ledGridTexture.createGraphics();
    drawLEDGrid(gs, state.LED_WIDTH_Max, state.LED_HEIGHT_Max, LED_in_PixelSize, LED_in_PixelNumber, LED_in_Str_Spacing, LED_Color.Grid_Color, LED_in_gridIsRound);
    gs.dispose();// 释放 Graphics 对象

    //输出渲染耗时，调试用
    state.LEDRenderTime = {//调试用，平均渲染耗时
        RenderTimes: 0, // 当前渲染次数
        totalRenderTime: 0, // 累计渲染时间
        maxRenderTime: 0 //最大渲染时间
    };
}

function dispose(ctx, state, train) {//列车消失时调用,清除列车状态并释放内存
    state.LED_in_dh.close();
    state.LED_HeadOut_dh.close();
    state.ledStrTexture = null;
    state.ledGridTexture = null;
    state.led_HeadOut_StrTexture = null;
    state.repaintLED = null;
    state.LEDlastTrainStatus = null;
    state.LEDlastNextStationIndex = null;
    state.LEDlastDestination = null;
    state.LED_in = null;
    state.LED_HeadOut = null;
}

function render(ctx, state, train) {//列车渲染每一帧调用
    let renderStartTime = Date.now();//记录开始时间，测试用

    //从jef的上海LCD那抄来的更新线路信息，如果lcd已更新此部分，则此处不会再次更新
    if (train.shouldRender() && train.shouldRenderDetail() && shouldRepaintLED(state, train)) {
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
        if (checkNextRoute(train, state.trainStatus, LED_ARV_showNextRoute)) {
            platformInfo = train.getAllPlatforms().get(train.getAllPlatformsNextIndex() + 1);
            stationInfoList = getStationInfoListNear3(train, state.trainStatus, train.getAllPlatformsNextIndex());
        }
        const routeInfo = getRouteInfo(train, state.trainStatus, platformInfo);
        if (stationInfoList.length > 0) {
            routeInfo.stationInfoList = stationInfoList;
        }
        if (!checkJsonProperty(state, "routeInfo", routeInfo)) { // 如果 state 中不存在 routeInfo 或 routeInfo 改变，则更新 routeInfo
            print("Train " + train.id() + " routeInfo：" + JSON.stringify(routeInfo));
            state.routeInfo = routeInfo;
        }
    }

    //绘制LED
    if (train.shouldRender() && train.shouldRenderDetail() && state.LED_RateLimit.shouldUpdate()) {
        let nextStationIndex = state.trainStatus == STATUS_RETURNING_TO_DEPOT ? 1 : train.getThisRoutePlatformsNextIndex();
        nextStationIndex = checkNextRoute(train, state.trainStatus, LED_ARV_showNextRoute) ? 0 : nextStationIndex;
        let g = state.LED_in_dh.graphicsFor("led_display_in");
        let gho = state.LED_HeadOut_dh.graphicsFor("led_display_HeadOut");

        //-------------------------------------------静--态--部--分--绘--制-------------------------------------------
        if (state.trainStatus == STATUS_NO_ROUTE || state.routeInfo == null) {// 无线路
            if (state.LEDlastTrainStatus != STATUS_NO_ROUTE) {
                let strToDraw = { strCjk: LED_in_Tips_NO_ROUTE.Cjk, strNonCjk: LED_in_Tips_NO_ROUTE.NonCjk, gap: 2 };
                state.LED_in = getLEDStr(g, LED_in_Str_WIDTH, strToDraw, UNIFONT_16PX, LED_in_Str_Height, LED_in_Str_WIDTH, true);

                strToDraw = { strCjk: LED_HeadOut_Tips_NO_ROUTE.Cjk, strNonCjk: LED_HeadOut_Tips_NO_ROUTE.NonCjk, gap: 2 };
                state.LED_HeadOut = getHeadLEDStr(g, LED_HeadOut_Str_WIDTH, strToDraw, UNIFONT_16PX, LED_HeadOut_Str_Height, false);
            }
        } else if (state.trainStatus == STATUS_RETURNING_TO_DEPOT) {//回库车，绘制回库提示
            if (state.LEDlastTrainStatus != STATUS_RETURNING_TO_DEPOT) {
                let strToDraw = { strCjk: LED_in_Tips_DEPOT.Cjk, strNonCjk: LED_in_Tips_DEPOT.NonCjk, gap: 2 };
                state.LED_in = getLEDStr(g, LED_in_Str_WIDTH, strToDraw, UNIFONT_16PX, LED_in_Str_Height, LED_in_Str_WIDTH, true);

                strToDraw = { strCjk: LED_HeadOut_Tips_DEPOT.Cjk, strNonCjk: LED_HeadOut_Tips_DEPOT.NonCjk, gap: 2 };
                state.LED_HeadOut = getHeadLEDStr(g, LED_HeadOut_Str_WIDTH, strToDraw, UNIFONT_16PX, LED_HeadOut_Str_Height, false);
            }
        } else if (state.routeInfo.stationInfoList.length && state.routeInfo.stationInfoList[nextStationIndex]) {
            //终点站变动则重绘车头LED
            if (state.routeInfo.destination && state.routeInfo.destination != state.LEDlastDestination) {
                let destination_Cjk = TextUtil.getCjkParts(state.routeInfo.destination) + "";
                let destination_NonCjk = TextUtil.getNonCjkParts(state.routeInfo.destination) + "";
                let strToDraw = { strCjk: destination_Cjk, strNonCjk: destination_NonCjk, gap: 2 };
                state.LED_HeadOut = getHeadLEDStr(g, LED_HeadOut_Str_WIDTH, strToDraw, UNIFONT_16PX, LED_HeadOut_Str_Height, LED_HeadOut_Str_WIDTH, false);
                state.LEDlastDestination = state.routeInfo.destination;
                state.RouteLength = getLineLength(train);
            }

            if (state.trainStatus == STATUS_ARRIVED && state.LEDlastTrainStatus != STATUS_ARRIVED) {//如果列车状态由 其他 转为 STATUS_ARRIVED 则更新到站提示信息
                let thisStationInfo = state.routeInfo.stationInfoList[nextStationIndex];
                let LED_interchangeStr = getLEDInterchangeStr(thisStationInfo);

                let LED_StrToDraw;
                if (nextStationIndex == state.routeInfo.stationInfoList.length - 1) {//如果到达终点站则显示终点站格式
                    LED_StrToDraw = getLEDArriveDestinationStr(thisStationInfo.stationName, LED_interchangeStr.interchange_CJK, LED_interchangeStr.interchange_NonCJK);
                } else {
                    LED_StrToDraw = getLEDThisStationStr(thisStationInfo.stationName, LED_interchangeStr.interchange_CJK, LED_interchangeStr.interchange_NonCJK);
                }

                state.LED_in = getLEDStr(g, LED_in_Str_WIDTH, LED_StrToDraw, UNIFONT_16PX, LED_in_Str_Height, LED_in_Str_WIDTH, true);
            } else if (state.trainStatus != STATUS_ARRIVED && nextStationIndex != state.LEDlastNextStationIndex) {//nextStationIndex2 改变则更新下一站信息
                let nextStationInfo = state.routeInfo.stationInfoList[nextStationIndex];
                let LED_interchangeStr = getLEDInterchangeStr(nextStationInfo);

                let LED_StrToDraw;
                if (nextStationIndex == state.routeInfo.stationInfoList.length - 1) {//如果到达终点站则显示终点站格式
                    LED_StrToDraw = getLEDToDestinationStr(state.routeInfo.routeName, state.routeInfo.destination, nextStationInfo.stationName, LED_interchangeStr.interchange_CJK, LED_interchangeStr.interchange_NonCJK, state.RouteLength);
                } else {
                    LED_StrToDraw = getLEDNextStationStr(state.routeInfo.routeName, state.routeInfo.destination, nextStationInfo.stationName, LED_interchangeStr.interchange_CJK, LED_interchangeStr.interchange_NonCJK, state.RouteLength);
                }

                state.LED_in = getLEDStr(g, LED_in_Str_WIDTH, LED_StrToDraw, UNIFONT_16PX, LED_in_Str_Height, LED_in_Str_WIDTH, true);
                state.LEDlastNextStationIndex = nextStationIndex;
            }
        }

        //绘制点阵图并存入 state.ledStrTexture 中
        if (state.LED_in.shouldRepaint) {
            state.ledStrTexture = new BufferedImage(state.LED_in.StrLength, LED_in_Str_Height, BufferedImage.TYPE_INT_ARGB);
            let gs = state.ledStrTexture.createGraphics();
            let strInfo = {
                Cjk_Str: state.LED_in.strCjk,
                Cjk_X: LED_in_Str_WIDTH,
                Cjk_Color: LED_Color.Text_In_Cjk_Color,
                NonCjk_Str: state.LED_in.strNonCjk,
                NonCjk_X: LED_in_Str_WIDTH + state.LED_in.strCjk_Length + state.LED_in.gap_Length,
                NonCjk_Color: LED_Color.Text_In_NonCjk_Color
            };
            drawLED(gs, 0, 0, state.LED_in.StrLength, LED_in_Str_Height, strInfo, LED_in_Str_Height, LED_Color.BG_Color, UNIFONT_16PX);
            gs.dispose();// 释放 Graphics 对象 

            state.LED_in.shouldRepaint = false;
            print("LED_in:" + JSON.stringify(state.LED_in, null, 2));
        }
        if (state.LED_HeadOut.shouldRepaint) {
            state.led_HeadOut_StrTexture = new BufferedImage(state.LED_HeadOut.StrLength, LED_HeadOut_Str_Height, BufferedImage.TYPE_INT_ARGB);
            let gs = state.led_HeadOut_StrTexture.createGraphics();
            let strInfo = {
                Cjk_Str: state.LED_HeadOut.strCjk,
                Cjk_X: state.LED_HeadOut.StrX_Cjk,
                Cjk_Color: LED_Color.Text_Out_Cjk_Color,
                NonCjk_Str: state.LED_HeadOut.strNonCjk,
                NonCjk_X: state.LED_HeadOut.strCjk_Length + state.LED_HeadOut.gap_Length + state.LED_HeadOut.StrX_NonCjk,
                NonCjk_Color: LED_Color.Text_Out_NonCjk_Color
            };
            drawLED(gs, 0, 0, state.LED_in.StrLength, LED_in_Str_Height, strInfo, LED_in_Str_Height, LED_Color.BG_Color, UNIFONT_16PX);
            gs.dispose();// 释放 Graphics 对象 

            print("LED_HeadOut:" + JSON.stringify(state.LED_HeadOut, null, 2));
        }
        //-------------------------------------------动--态--部--分--绘--制-------------------------------------------
        //车内 LED 部分
        if (state.ledStrTexture && state.LED_in) {
            g.drawImage(state.ledStrTexture, state.LED_in.StrX, 0, state.LED_in.StrLength, LED_in_Str_Height, null);//绘制滚动字符
            g.drawImage(state.ledGridTexture, 0, 0, state.LED_WIDTH_Max, state.LED_HEIGHT_Max, null);//绘制网格
            state.LED_in.StrX -= LEDRollSpeed;
            if (state.LED_in.StrX <= -(state.LED_in.StrLength + LED_in_Str_Spacing) + LED_in_Str_WIDTH) {
                state.LED_in.StrX = 0;
            }
            state.LED_in_dh.upload();
        }

        //车头外 LED 部分
        if (state.led_HeadOut_StrTexture && state.LED_HeadOut) {
            if (state.LED_HeadOut.shouldRoll) {//滚动播放

                //前后停滞
                let StrX = state.LED_HeadOut.StrX + LED_HeadOut_Str_WIDTH;
                if (state.LED_HeadOut.StrX > -LED_HeadOut_Str_WIDTH) {
                    StrX = 0;
                } else if (state.LED_HeadOut.StrX <= -state.LED_HeadOut.StrLength) {
                    StrX = -state.LED_HeadOut.StrLength + LED_HeadOut_Str_WIDTH;
                }
                gho.drawImage(state.led_HeadOut_StrTexture, StrX, 0, state.LED_HeadOut.StrLength, LED_HeadOut_Str_Height, null);//绘制滚动字符
                gho.drawImage(state.ledGridTexture, 0, 0, state.LED_WIDTH_Max, state.LED_HEIGHT_Max, null);//绘制网格
                state.LED_HeadOut.StrX -= LEDRollSpeed;

                //重置位移
                if (state.LED_HeadOut.StrX <= -(state.LED_HeadOut.StrLength + LED_in_Str_Spacing) - LED_HeadOut_Str_WIDTH) {
                    state.LED_HeadOut.StrX = 0;
                }
                state.LED_HeadOut_dh.upload();
            } else if (state.LED_HeadOut.shouldRepaint) {//居中固定播放
                gho.setColor(LED_Color.BG_Color);
                gho.fillRect(0, 0, LED_HeadOut_WIDTH, LED_HeadOut_HEIGHT);
                gho.drawImage(state.led_HeadOut_StrTexture, state.LED_HeadOut.StrX, 0, state.LED_HeadOut.StrLength, LED_HeadOut_Str_Height, null);//绘制静态居中字符
                gho.drawImage(state.ledGridTexture, 0, 0, state.LED_WIDTH_Max, state.LED_HEIGHT_Max, null);//绘制网格(此处偷懒用的车内LED的网格)
                state.LED_HeadOut_dh.upload();
            }
            state.LED_HeadOut.shouldRepaint = false;
        }

        state.LEDlastTrainStatus = state.trainStatus;//保存本次列车状态
    }//LED绘制结束

    //渲染模型部分(ps: 当车厢数小于2时会出渲染bug)
    for (let i = 0; i < train.trainCars(); i++) {
        if (i == 0) {//头车
            ctx.drawCarModel(state.LED_in_dh.model, i, state.matrix_disply_F1);
            ctx.drawCarModel(state.LED_in_dh.model, i, state.matrix_disply_F3);
            ctx.drawCarModel(state.LED_HeadOut_dh.model, i, state.matrix_head_out_disply_F);
        } else if (i == train.trainCars() - 1) {//尾车
            ctx.drawCarModel(state.LED_in_dh.model, i, state.matrix_disply_R1);
            ctx.drawCarModel(state.LED_in_dh.model, i, state.matrix_disply_R3);
            ctx.drawCarModel(state.LED_HeadOut_dh.model, i, state.matrix_head_out_disply_R);
        } else {//中间车
            ctx.drawCarModel(state.LED_in_dh.model, i, state.matrix_disply_M1);
            ctx.drawCarModel(state.LED_in_dh.model, i, state.matrix_disply_M2);
        }
    }

    let renderEndTime = Date.now();// 记录渲染结束时间
    LEDRenderTime(state, renderStartTime, renderEndTime);//输出渲染时间，调试用
}

function shouldRepaintLED(state, train) {
    let trainStatus = getTrainStatus(train);
    if (checkProperty(state, "trainStatus", trainStatus)) { // 如果 state 中存在 trainStatus 且符合当前状态
        if (state.repaintLED) {
            state.repaintLED = false;
            return true;
        }
        return false;
    } else {
        state.trainStatus = trainStatus;
        print("trainStatus:" + trainStatus);
        return true;
    }
}

function LEDRenderTime(state, renderStartTime, renderEndTime) {
    let renderTime = renderEndTime - renderStartTime;// 计算渲染一帧的时间
    // 更新状态
    state.LEDRenderTime.RenderTimes += 1; // 渲染次数加 1
    state.LEDRenderTime.totalRenderTime += renderTime; // 累计渲染时间
    if (renderTime > state.LEDRenderTime.maxRenderTime) {// 最大渲染时间
        state.LEDRenderTime.maxRenderTime = renderTime;
    }
    // 检查是否达到 500 次渲染，是则输出平均渲染耗时
    if (state.LEDRenderTime.RenderTimes >= 1000) {
        let averageRenderTime = state.LEDRenderTime.totalRenderTime / state.LEDRenderTime.RenderTimes; // 计算平均渲染时间
        averageRenderTime = averageRenderTime.toFixed(3);
        print("KMLED:{averageRenderTime:" + averageRenderTime + "ms,totalRenderTime:" + state.LEDRenderTime.totalRenderTime + "ms,maxRenderTime:" + state.LEDRenderTime.maxRenderTime + "ms}");
        state.LEDRenderTime.RenderTimes = 0;
        state.LEDRenderTime.totalRenderTime = 0;
        state.LEDRenderTime.maxRenderTime = 0;
    }
}

// 绘制网格 模拟 LED 点阵
function drawLEDGrid(g, width, height, PixelSize, PixelNumber, spacing, grid_Color, gridIsRound) {
    // 设置抗锯齿以提高圆形显示质量
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(grid_Color);

    if (gridIsRound) {//是否绘制圆形，否则仅绘制方形
        // 设置线条宽度
        let originalStroke = g.getStroke();
        let lineStroke = new BasicStroke(spacing, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g.setStroke(lineStroke);

        // 绘制点阵
        for (let row = 0; row < PixelNumber; row++) {
            for (let col = 0; col < width / PixelSize; col++) {
                // 计算圆形点的左上坐标
                let circleX = col * PixelSize + spacing / 2;
                let circleY = row * PixelSize + spacing / 2;
                g.drawOval(circleX, circleY, PixelSize, PixelSize);// 绘制圆形边缘
            }
        }
        g.setStroke(originalStroke);// 恢复原来的笔划设置
    }

    //绘制网格
    for (let i = 0; i <= width / PixelSize; i++) {//绘制竖排网格
        g.fillRect(i * PixelSize, 0, spacing, height);
    }
    for (let i = 0; i < PixelNumber; i++) {//绘制横排网格
        g.fillRect(0, i * PixelSize, width, spacing);
    }
    g.fillRect(0, PixelSize * PixelNumber, width, height - PixelSize * PixelNumber);//绘制第17行
    // 关闭抗锯齿
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
}

// 绘制点阵 LED
function drawLED(g, x, y, width, height, strInfo, fontSize, BG_Color, fontToDraw) {
    //清屏
    g.setColor(BG_Color);
    g.fillRect(x, y, width, height);

    // 绘制点阵字符
    let font = fontToDraw.deriveFont(fontSize);
    let fm = g.getFontMetrics(font);
    g.setFont(font);

    if (strInfo.Cjk_Str) {
        g.setColor(strInfo.Cjk_Color);
        g.drawString(strInfo.Cjk_Str, strInfo.Cjk_X, fm.getAscent());
    }
    if (strInfo.NonCjk_Str) {
        g.setColor(strInfo.NonCjk_Color);
        g.drawString(strInfo.NonCjk_Str, strInfo.NonCjk_X, fm.getAscent());
    }
}

//获取LED显示内容，计算文本长度及初始化
// shouldRoll 是否强制滚动
function getLEDStr(g, width, strToDraw, fontToDraw, fontSize, blank, shouldRoll) {
    //计算文本长度
    let fm = g.getFontMetrics(fontToDraw.deriveFont(fontSize));
    let strCjk_Length = strToDraw.strCjk ? fm.stringWidth(strToDraw.strCjk) : 0;
    strCjk_Length = Math.ceil(strCjk_Length / fontSize) * fontSize;//向上取整
    let strNonCjk_Length = strToDraw.strNonCjk ? fm.stringWidth(strToDraw.strNonCjk) : 0;
    strNonCjk_Length = Math.ceil(strNonCjk_Length / fontSize) * fontSize;//向上取整
    let gap_Length = strCjk_Length && strNonCjk_Length ? strToDraw.gap * fontSize : 0;
    let StrLength = strCjk_Length + strNonCjk_Length + gap_Length;
    let StrX = 0;//初始化 LED 滚动位置
    if (StrLength > width) {//超长就滚动
        StrLength += 2 * blank;//设置前后两段文本之间的空隙
        shouldRoll = true;
    } else {
        StrX = Math.ceil((width - StrLength) / 2 / LED_in_PixelSize) * LED_in_PixelSize;//计算起始值并取整
    }
    return {
        strCjk: strToDraw.strCjk,
        strNonCjk: strToDraw.strNonCjk,
        strCjk_Length: strCjk_Length,
        strNonCjk_Length: strNonCjk_Length,
        gap_Length: gap_Length,
        StrLength: StrLength,
        shouldRepaint: true,
        StrX: StrX,
        shouldRoll: shouldRoll
    }
}

//获取车头LED显示内容，计算文本长度及初始化
// shouldRoll 是否强制滚动
function getHeadLEDStr(g, width, strToDraw, fontToDraw, fontSize, shouldRoll) {
    //计算文本长度
    let fm = g.getFontMetrics(fontToDraw.deriveFont(fontSize));
    let strCjk_Length = strToDraw.strCjk ? fm.stringWidth(strToDraw.strCjk) : 0;
    strCjk_Length = Math.ceil(strCjk_Length / fontSize) * fontSize;//向上取整
    let strNonCjk_Length = strToDraw.strNonCjk ? fm.stringWidth(strToDraw.strNonCjk) : 0;
    strNonCjk_Length = Math.ceil(strNonCjk_Length / fontSize) * fontSize;//向上取整
    let gap_Length = strCjk_Length > width && strNonCjk_Length > width ? strToDraw.gap * fontSize : 0;

    let StrX_Cjk = 0, StrX_NonCjk = 0;
    if (strCjk_Length != 0 && strCjk_Length < width) {
        StrX_Cjk = Math.ceil((width - strCjk_Length) / 2 / LED_in_PixelSize) * LED_in_PixelSize;
        strCjk_Length = width;
    }
    if (strNonCjk_Length != 0 && strNonCjk_Length < width) {
        StrX_NonCjk = Math.ceil((width - strNonCjk_Length) / 2 / LED_in_PixelSize) * LED_in_PixelSize;
        strNonCjk_Length = width;
    }

    let StrLength = strCjk_Length + strNonCjk_Length + gap_Length;
    shouldRoll = shouldRoll || StrLength > width;

    return {
        strCjk: strToDraw.strCjk,
        strNonCjk: strToDraw.strNonCjk,
        strCjk_Length: strCjk_Length,
        strNonCjk_Length: strNonCjk_Length,
        gap_Length: gap_Length,
        StrX_Cjk: StrX_Cjk,
        StrX_NonCjk: StrX_NonCjk,
        StrLength: StrLength,
        shouldRepaint: true,
        StrX: 0,//初始化 LED 滚动位置
        shouldRoll: shouldRoll
    }
}

//获取LED换乘信息字符串
function getLEDInterchangeStr(nextStationInfo) {
    let interchangeInfo = nextStationInfo.interchangeInfo;
    let StationNameCJK = TextUtil.getCjkParts(nextStationInfo.stationName);
    let StationNameNonCJK = TextUtil.getNonCjkParts(nextStationInfo.stationName);

    //拼接中文下一站换乘信息，如果没有则 interchange_CJK 为 ""
    let interchange_CJK = "";
    if (interchangeInfo.length > 0 && !!StationNameCJK) {// 使用 map 方法处理换乘信息数组
        interchange_CJK += "，可换乘";// 添加换乘标志 "可换乘"
        interchange_CJK += interchangeInfo.map((info, index) => {
            return (index > 0 ? "，" : "") + TextUtil.getCjkParts(info.name);//添加分隔符"，"
        }).join('');
    }

    //拼接英文下一站换乘信息，如果没有则 interchange_NonCJK 为 ""
    let interchange_NonCJK = "";
    if (interchangeInfo.length > 0 && !!StationNameNonCJK) {// 使用 map 方法处理换乘信息数组，\u00A0 是空格
        interchange_NonCJK += ",\u00A0Passengers can transfer to\u00A0";// 添加换乘标志 "Transfer to"
        interchange_NonCJK += interchangeInfo.map((info, index) => {
            return (index > 0 ? ",\u00A0" : "") + TextUtil.getNonCjkParts(info.name);//添加分隔符", "
        }).join('');
    }
    return {
        interchange_CJK: interchange_CJK,
        interchange_NonCJK: interchange_NonCJK
    }
}

//获取 线路全程长度
function getLineLength(train) {
    let PlatformInfos = train.getThisRoutePlatforms();
    let platform_start = PlatformInfos.get(0).distance;
    let platform_end = PlatformInfos.get(PlatformInfos.size() - 1).distance;
    return platform_end - platform_start;//全长，单位m
}