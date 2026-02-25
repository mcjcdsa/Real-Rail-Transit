importPackage(java.awt);
importPackage(java.awt.font);
importPackage(java.awt.geom);
importPackage(java.awt.image);

//绘制参数
const LDM_Logo_Tex = Resources.readBufferedImage(Resources.idr("logo.png"));//logo贴图
const LDM_Logo_X = 0;//logo位置
const LDM_Logo_Y = 0;
const LDM_Logo_W = Math.ceil(0.15 * LDM_WIDTH);
const LDM_Logo_H = Math.ceil(0.22 * LDM_HEIGHT);

const LDM_Phone_Tex = Resources.readBufferedImage(Resources.idr("phone.png"));//电话贴图
const LDM_Phone_X = 0;
const LDM_Phone_Y = Math.ceil(0.83 * LDM_HEIGHT);
const LDM_Phone_W = Math.ceil(0.1 * LDM_WIDTH);
const LDM_Phone_H = Math.ceil(0.15 * LDM_HEIGHT);

const LDM_Side_W = Math.ceil(0.1 * LDM_WIDTH);//左右留白部分宽度
const LDM_Line_h = Math.ceil(0.066 * LDM_HEIGHT);//线路线条宽度
const LDM_Line_TS = Math.ceil(0.45 * LDM_Line_h);//站名小箭头边长

const LDM_Line_R1 = 0.6 * LDM_Line_h;//线路站点圆形直径1(内环)
const LDM_Line_R1b = 0 * LDM_Line_R1;//线路站点圆形直径1(内环)黑边
const LDM_Line_R2 = 2.25 * LDM_Line_h;//线路站点圆形直径2(中环)
const LDM_Line_R2b = 0.7 * LDM_Line_R2;//线路站点圆形直径2(中环)黑边
const LDM_Line_R3 = 2.25 * LDM_Line_h;//线路站点圆形直径3(外环)
const LDM_Line_R3b = 0.9 * LDM_Line_R3;//线路站点圆形直径3(外环)黑边
const LDM_IC_CCH = Math.ceil(3.2 * LDM_Line_h);//换乘箭头中心与站点中心距离

const LDM_Line_TR = (LDM_Line_R3b + LDM_Line_R1) / 2;//换乘箭头环直径
const LDM_lineWidth = 0.15 * (LDM_Line_R3b - LDM_Line_R1);//换乘箭头环的箭头尾巴宽度

const LDM_Station_L = 0.1 * LDM_WIDTH;//站点名长
const LDM_Station_W = 0.18 * LDM_HEIGHT;//站点名宽

const LDM_LT_R1 = LDM_Line_R1 / LDM_AF;//灯光部分灯泡大小
const LDM_LT_R2 = LDM_Line_R2 / LDM_AF;//灯光部分灯泡大小
const LDM_LT_R3 = LDM_Line_R3 / LDM_AF;//灯光部分灯泡大小

const LDM_Up_H = Math.ceil(0 * LDM_Line_h);//上部留白部分宽度
const LDM_Up_X1 = Math.ceil(0.70 * LDM_WIDTH);//上部区域 开关门灯位置1
const LDM_Up_X2 = Math.ceil(0.85 * LDM_WIDTH);//上部区域 开关门灯位置2

//线路图绘制
function drawLDMInterior(g, routeColor, stationInfoList, directionToDraw, LDM_MAP_INFO, listStationInterchange) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//启用抗锯齿
    //背景
    g.setColor(LDM_MAP_INFO.backgroundColor);//白偏灰色
    g.fillRect(0, 0, LDM_WIDTH, LDM_HEIGHT);

    //logo及提示
    g.drawImage(LDM_Logo_Tex, LDM_Logo_X, LDM_Logo_Y, LDM_Logo_W, LDM_Logo_H, null);
    g.drawImage(LDM_Phone_Tex, LDM_Phone_X, LDM_Phone_Y, LDM_Phone_W, LDM_Phone_H, null);

    //画作者
    let font = LDM_FONT_CJK.deriveFont(12.0);
    let fm = g.getFontMetrics(font);
    g.setColor(Color.GRAY);
    g.setFont(font);
    g.drawString(WH_AUTHOR, LDM_WIDTH - fm.stringWidth(WH_AUTHOR) - 10, LDM_HEIGHT - fm.getHeight());

    //算参数
    let AC_int = (LDM_WIDTH - 2 * LDM_Side_W) / (stationInfoList.length + 1);//每个站点间隔
    let line_AF = 1;//灯珠缩放比例
    if (AC_int < 1.2 * LDM_Line_R3) {//如果水平方向上间隔不足，则缩小比例
        line_AF = AC_int / LDM_Line_R3 / 1.2;
    }
    let station_L = Math.min(Math.floor(0.9 * AC_int), LDM_Station_L);//站名长
    let station_W = Math.min(LDM_Station_W, 0.18 * (LDM_HEIGHT - LDM_Up_H));//站名高

    //如果开启统一字体大小，则计算最小字体大小
    let uniformFontSize_Cjk = 0;
    let uniformFontSize_NonCjk = 0;
    let isDL = CheckDL(stationInfoList);
    if (LDM_MAP_INFO.isUniformFontSize) {
        uniformFontSize_Cjk = 10000;
        uniformFontSize_NonCjk = 10000;
        let station_W_Cjk = station_W;
        let station_W_NonCjk = station_W;
        if (isDL) {//检查是否双语（含"|"），是则73分
            station_W_Cjk = 0.75 * station_W;
            station_W_NonCjk = 0.25 * station_W;
        }

        for (let i = 0; i < stationInfoList.length; i++) {//循环站名
            if (!stationInfoList[i] || !stationInfoList[i].stationName) {
                throw new Error("stationInfoList" + i + " is undefined or invalid.");
            }

            let stationName = stationInfoList[i].stationName;
            if (isDL) {//双行绘制
                let stationName_Cjk = TextUtil.getCjkParts(stationName);
                let stationName_NonCjk = TextUtil.getNonCjkParts(stationName);
                let fontSize = 10000;
                if (!stationName_Cjk.isEmpty()) {
                    fontSize = calculateMaxFontSizeN(g, LDM_FONT_CJK, stationName_Cjk, station_L, station_W_Cjk, 0, 0);
                    uniformFontSize_Cjk = Math.min(uniformFontSize_Cjk, fontSize);
                }
                if (!stationName_NonCjk.isEmpty()) {
                    fontSize = calculateMaxFontSizeN(g, LDM_FONT_NONCJK, stationName_NonCjk, station_L, station_W_NonCjk, 0, 0);
                    uniformFontSize_NonCjk = Math.min(uniformFontSize_NonCjk, fontSize);
                }
            } else {//单行绘制
                let isCjk = CheckCJK(stationName);
                let strToDraw = getMatching(stationName, isCjk);
                if (!!strToDraw) {
                    let fontToDraw = isCjk ? LDM_FONT_CJK : LDM_FONT_NONCJK;
                    let fontSize = calculateMaxFontSizeN(g, fontToDraw, strToDraw, station_L, station_W, 0, 0);
                    uniformFontSize_Cjk = Math.min(uniformFontSize_Cjk, fontSize);
                    uniformFontSize_NonCjk = uniformFontSize_Cjk;
                }
            }
        }
    }

    //绘制线路直线
    let ys = (LDM_HEIGHT + LDM_Up_H) / 2;//线路线条y值(中心)
    g.setColor(routeColor);
    g.fillRect(AC_int - LDM_Line_h / 2 + LDM_Side_W, ys - LDM_Line_h / 2, LDM_WIDTH - 2 * AC_int + LDM_Line_h - 2 * LDM_Side_W, LDM_Line_h);
    g.fillOval(AC_int - LDM_Line_h + LDM_Side_W, ys - LDM_Line_h / 2, LDM_Line_h, LDM_Line_h);//左半圆
    g.fillOval(LDM_WIDTH - AC_int - LDM_Side_W, ys - LDM_Line_h / 2, LDM_Line_h, LDM_Line_h);//右半圆

    //画站点名称、灯珠环和换乘信息
    let xs = directionToDraw ? AC_int + LDM_Side_W : LDM_WIDTH - AC_int - LDM_Side_W;
    let IC_IntW = 0.4 * LDM_Line_h, IC_IntH = 0.6 * LDM_IC_CCH;//换乘线路延伸线条
    let IC_cy = ys + LDM_IC_CCH;//换乘箭头中心Y坐标
    let IC_SLW = Math.min(0.9 * station_L, 1.3 * LDM_Line_R2 * line_AF);//换乘箭头限宽
    let IC_NY = Math.ceil(IC_cy + 0.75 * LDM_Line_R2);
    let IC_H = Math.ceil(LDM_HEIGHT - IC_NY - 0.02 * LDM_HEIGHT);//换乘线路名称高100
    let IC_maxW = 0.95 * station_L;//换乘线路名称最大宽度
    let IC_NTW = Math.ceil(1.5 * IC_H);//换乘线路名称最适宜宽度
    let dH = LDM_Line_h * 0.75 * line_AF;
    for (let i = 0; i < stationInfoList.length; i++) {
        let isUp = true;//(!directionToDraw && (stationInfoList.length % 2 == 0)) == (i % 2 == 0);
        let interchangeRoute = stationInfoList[i].interchangeInfo;

        //绘制站点换乘信息和圆形
        if (interchangeRoute != null && interchangeRoute.length > 0) {//若本站是换乘站，绘制换乘信息
            //绘制延伸线条
            let is1IC = interchangeRoute.length == 1;//只有1个换乘站
            let ICAngle = 360 / (interchangeRoute.length + 1);//切份数
            g.setColor(routeColor);
            g.fillRect(xs - IC_IntW / 2, ys, IC_IntW, IC_IntH + (is1IC ? dH * 0.4 : 0));

            //绘制站点圆环
            drawLDMStnCircle(g, xs, ys, LDM_Line_R2, LDM_Line_R2b, Color.BLACK);
            g.fillOval(xs - LDM_Line_R1 / 2, ys - LDM_Line_R1 / 2, LDM_Line_R1, LDM_Line_R1);//中心黑点

            //绘制换乘箭头
            let yc = is1IC ? IC_cy + dH : IC_cy;
            let IC_ARLW = (is1IC ? 0.9 : 0.7) * IC_SLW;
            let IC_ARW = IC_ARLW * 0.66 * 2, IC_ARH = IC_ARLW * 0.82 * 2;//换乘箭头实宽、高
            drawArrowOnCircleNN(g, xs, yc, IC_ARW, IC_ARH, 0, routeColor);//本线绘制
            if (is1IC) yc = IC_cy - dH;
            let IC_AF = 1, IC_NW = IC_NTW, IC_NH = IC_H;
            if ((interchangeRoute.length * IC_NTW) > IC_maxW) {//计算换乘线路名称区域大小
                IC_AF = IC_maxW / interchangeRoute.length / IC_NTW;
                IC_NW = Math.ceil(IC_AF * IC_NTW);
                IC_NH = Math.ceil(IC_AF * IC_H);
            }
            let IC_NX = xs - IC_NW * interchangeRoute.length / 2;
            let gap = Math.ceil(0.025 * IC_NW), IC_NDX = IC_NX + gap, IC_NDY = IC_NY + gap, IC_NDW = IC_NW - 2 * gap, IC_NDH = IC_NH - 2 * gap;
            for (let j = 0; j < interchangeRoute.length; j++) {
                let interchange = interchangeRoute[j];
                drawArrowOnCircleNN(g, xs, yc, IC_ARW, IC_ARH, (j + 1) * ICAngle, interchange.color);//换乘线绘制

                //绘制换乘线路名称
                drawLDMInterChangeName(g, IC_NDX, IC_NDY, IC_NDW, IC_NDH, interchange.name, interchange.color);
                IC_NDX += IC_NW;
            }

            //换乘文本
            drawSingleLineText(g, "换乘", xs - LDM_Line_R2b / 2, IC_cy - LDM_Line_R2b / 2, LDM_Line_R2b, LDM_Line_R2b, Color.BLACK, true, LDM_FONT_CJK, 0);
        } else {//无换乘只绘制灯珠圆环
            drawLDMStnCircle(g, xs, ys, LDM_Line_R2, LDM_Line_R2b, routeColor);
        }

        //绘制本站站名
        let ysd = isUp ? -1 : 1;
        let ysm = ys + ysd * (LDM_Line_h + LDM_Line_R2) / 2;//线路名下沿距ys的偏心值
        if (isDL) {//双行绘制
            let stationName_Cjk = TextUtil.getCjkParts(stationInfoList[i].stationName) + "";
            let stationName_NonCjk = TextUtil.getNonCjkParts(stationInfoList[i].stationName) + "";
            let sh_Cjk = LDM_MAP_INFO.isUniformFontSize ? uniformFontSize_Cjk : 0.7 * station_W;
            let sh_NonCjk = LDM_MAP_INFO.isUniformFontSize ? uniformFontSize_NonCjk : 0.3 * station_W;

            drawHorizontalText(g, stationName_Cjk, xs, isUp ? ysm - sh_NonCjk : ysm, station_L, sh_Cjk, Color.BLACK, LDM_FONT_CJK, true, uniformFontSize_Cjk, false, isUp ? 90 : 270, 0, null, null);
            drawHorizontalText(g, stationName_NonCjk, xs, isUp ? ysm : ysm + sh_Cjk, station_L, sh_NonCjk, Color.BLACK, LDM_FONT_NONCJK, false, uniformFontSize_NonCjk, false, isUp ? 90 : 270, 0, null, null);
        } else {
            let isCjk = CheckCJK(stationInfoList[i].stationName);
            let strToDraw = getMatching(stationInfoList[i].stationName, isCjk);
            let fontToDraw = isCjk ? LDM_FONT_CJK : LDM_FONT_NONCJK;
            drawHorizontalText(g, strToDraw, xs, ysm, station_L, station_W, Color.BLACK, fontToDraw, isCjk, uniformFontSize_Cjk, false, isUp ? 90 : 270, 0, null, null);
        }

        //根据绘制方向调整间距
        xs += directionToDraw ? AC_int : -AC_int;
    }

    //绘制开关门提示部分
    if (LDM_Side_W != 0) {
        let Arrow_Length = (LDM_HEIGHT - ys) * 0.6;
        let Arrow_Width = LDM_Side_W * 0.25;
        let Arrow_x = LDM_Side_W / 2;
        let Arrow_y = ys + Arrow_Length / 2;
        let Arrow_BlackSide = Arrow_Width * 0.03;
        let DOORTIPS_Height = 0.45 * (ys - LDM_Up_H);
        let DOORTIPS_y = ys - DOORTIPS_Height * 1.2;
        let DOORTIPS_Cjk_h = 0.6 * DOORTIPS_Height;

        //右侧箭头
        Arrow_x = LDM_WIDTH - LDM_Side_W / 2;
        drawStrokeArrow(g, Arrow_x, Arrow_y, 90, Color.BLACK, null, Arrow_Length, Arrow_Width, Arrow_BlackSide);
        //右侧文字
        drawSingleLineText(g, getMatching(LDM_DOORTIPS_OPN, true), LDM_WIDTH - LDM_Side_W, DOORTIPS_y, LDM_Side_W, DOORTIPS_Cjk_h, Color.BLACK, true, LDM_FONT_CJK, 0);
        drawSingleLineText(g, getMatching(LDM_DOORTIPS_OPN, false), LDM_WIDTH - LDM_Side_W, DOORTIPS_y + DOORTIPS_Cjk_h, LDM_Side_W, DOORTIPS_Height - DOORTIPS_Cjk_h, Color.BLACK, true, LDM_FONT_NONCJK, 0);
    }

    return {
        AC_int: AC_int,
        AF: line_AF,
        door_xs1: LDM_Up_X1 + LDM_Line_R2 / 2,
        door_xs2: LDM_Up_X2 + LDM_Line_R2 / 2,
        door_ys: 0.6 * LDM_Up_H
    }
}

//灯光静态部分
function drawLDMLTSP(g, LDM_MAP_INFO, LDM_Info, directionToDraw, nextStationIndex, station_Total) {
    drawLDMLTBG(g, LDM_LT_WIDTH, LDM_LT_HEIGHT, LDM_MAP_INFO.LTBackgroundColor);//清屏

    let LT_r = LDM_LT_R3 * LDM_Info.AF;
    let ys = (LDM_LT_HEIGHT + LDM_Up_H / LDM_AF) / 2;
    for (let i = nextStationIndex; i < station_Total; i++) {//只绘制后续站点灯珠
        let xs = directionToDraw ? LDM_Info.AC_int * (1 + i) + LDM_Side_W / LDM_AF : LDM_LT_WIDTH - LDM_Info.AC_int * (1 + i) - LDM_Side_W / LDM_AF;
        if (i == nextStationIndex) {
            g.setColor(LDM_MAP_INFO.lightColor_NextFirstStnMain);
        } else {
            g.setColor(LDM_MAP_INFO.lightColor_NextStnMain);
        }
        g.fillRect(xs - LT_r / 2, ys - LT_r / 2, LT_r, LT_r);
    }
    //两侧门灯
    if (LDM_Side_W != 0) {
        let LDM_LT_Up_H = LDM_Up_H / LDM_AF;
        let LDM_LT_Side_W = LDM_Side_W / LDM_AF;
        let LDM_LT_Stoke = LDM_LT_Side_W * 0.05;
        g.setColor(LDM_MAP_INFO.DoorArrowOffColor);
        g.fillRect(LDM_LT_Stoke, LDM_LT_Up_H + LDM_LT_Stoke, LDM_LT_Side_W - LDM_LT_Stoke, LDM_LT_HEIGHT - LDM_LT_Up_H - 2 * LDM_LT_Stoke);
        g.fillRect(LDM_LT_WIDTH - LDM_LT_Side_W, LDM_LT_Up_H + LDM_LT_Stoke, LDM_LT_Side_W - LDM_LT_Stoke, LDM_LT_HEIGHT - LDM_LT_Up_H - 2 * LDM_LT_Stoke);
    }
}

//灯光动态部分 到站
function drawLDMLTTS(g, LDM_MAP_INFO, LDM_Info, directionToDraw, nextStationIndex, station_Total, LDM_Flash_RateLimit, LDM_FlashNextStn_RateLimit, displayState) {
    if (LDM_Flash_RateLimit.shouldUpdate()) {//更新闪烁计数器
        displayState.lightOn = !displayState.lightOn;
    }

    let LT_r = LDM_LT_R3 * LDM_Info.AF;
    let ys = (LDM_LT_HEIGHT + LDM_Up_H / LDM_AF) / 2;
    if (nextStationIndex < station_Total) {//绘制闪烁
        let xs = directionToDraw ? LDM_Info.AC_int * (nextStationIndex + 1) + LDM_Side_W / LDM_AF : LDM_LT_WIDTH - LDM_Info.AC_int * (nextStationIndex + 1) - LDM_Side_W / LDM_AF;
        if (!LDM_MAP_INFO.shouldFlashThisStn || displayState.lightOn) {
            g.setColor(LDM_MAP_INFO.lightColor_ThisStn);
        } else {
            g.setColor(LDM_MAP_INFO.LTBackgroundColor);
        }
        g.fillRect(xs - LT_r / 2, ys - LT_r / 2, LT_r, LT_r);
    }

    //后续站闪烁
    if (LDM_FlashNextStn_RateLimit.shouldUpdate() && (nextStationIndex < (station_Total - 1))) {
        if (displayState.lightNextStnCont == null) {
            displayState.lightNextStnCont = -1;
        }
        displayState.lightNextStnCont++;
        if (displayState.lightNextStnCont > station_Total) {
            displayState.lightNextStnCont = nextStationIndex + 1;
        }
    }

    //后续站灯泡闪烁
    if (nextStationIndex < (station_Total - 1)) {//绘制闪烁
        for (let i = nextStationIndex + 1; i < station_Total; i++) {
            let xs = directionToDraw ? LDM_Info.AC_int * (i + 1) + LDM_Side_W / LDM_AF : LDM_LT_WIDTH - LDM_Info.AC_int * (i + 1) - LDM_Side_W / LDM_AF;
            if (i == displayState.lightNextStnCont) {
                g.setColor(LDM_MAP_INFO.LTBackgroundColor);//灯灭
            } else {
                g.setColor(LDM_MAP_INFO.lightColor_NextStnMain);//全亮灯
            }
            g.fillRect(xs - LT_r / 2, ys - LT_r / 2, LT_r, LT_r);
        }
    }
}

//灯光动态部分 进行中
function drawLDMLTNS(g, LDM_MAP_INFO, LDM_Info, directionToDraw, nextStationIndex, station_Total, LDM_Flash_RateLimit, LDM_FlashNextStn_RateLimit, displayState) {

    if (LDM_Flash_RateLimit.shouldUpdate()) {//更新闪烁计数器
        displayState.lightOn = !displayState.lightOn;
    }


    //下一站灯泡闪烁
    let LT_r = LDM_LT_R3 * LDM_Info.AF;
    let ys = (LDM_LT_HEIGHT + LDM_Up_H / LDM_AF) / 2;
    if (nextStationIndex < station_Total) {//绘制闪烁
        let xs = directionToDraw ? LDM_Info.AC_int * (nextStationIndex + 1) + LDM_Side_W / LDM_AF : LDM_LT_WIDTH - LDM_Info.AC_int * (nextStationIndex + 1) - LDM_Side_W / LDM_AF;
        if (!LDM_MAP_INFO.shouldFlashNextFirstStn || displayState.lightOn) {
            g.setColor(LDM_MAP_INFO.lightColor_NextFirstStnMain);
        } else {
            g.setColor(LDM_MAP_INFO.LTBackgroundColor);
        }
        g.fillRect(xs - LT_r / 2, ys - LT_r / 2, LT_r, LT_r);
    }

    //后续站闪烁
    if (LDM_FlashNextStn_RateLimit.shouldUpdate() && (nextStationIndex < (station_Total - 1))) {
        if (displayState.lightNextStnCont == null) {
            displayState.lightNextStnCont = -1;
        }
        displayState.lightNextStnCont++;
        if (displayState.lightNextStnCont > station_Total) {
            displayState.lightNextStnCont = nextStationIndex + 1;
        }
    }

    //后续站灯泡闪烁
    if (nextStationIndex < (station_Total - 1)) {//绘制闪烁
        for (let i = nextStationIndex + 1; i < station_Total; i++) {
            let xs = directionToDraw ? LDM_Info.AC_int * (i + 1) + LDM_Side_W / LDM_AF : LDM_LT_WIDTH - LDM_Info.AC_int * (i + 1) - LDM_Side_W / LDM_AF;
            if (i == displayState.lightNextStnCont) {
                g.setColor(LDM_MAP_INFO.LTBackgroundColor);//灯灭
            } else {
                g.setColor(LDM_MAP_INFO.lightColor_NextStnMain);//全亮灯
            }
            g.fillRect(xs - LT_r / 2, ys - LT_r / 2, LT_r, LT_r);
        }
    }
}

//开关门闪灯提示
function drawLDMLTDoorInfo(g, LDM_MAP_INFO, LDM_Info, displayState, isRight, LDM_DoorState) {
    if (LDM_Up_H != 0) {
        if (LDM_DoorState.stateNow() == "off") {
            g.setColor(LDM_MAP_INFO.LTBackgroundColor);
            g.fillRect(LDM_Info.door_xs2 - LDM_LT_R3 / 2, LDM_Info.door_ys - LDM_LT_R3 / 2, LDM_LT_R3, LDM_LT_R3);
            g.fillRect(LDM_Info.door_xs1 - LDM_LT_R3 / 2, LDM_Info.door_ys - LDM_LT_R3 / 2, LDM_LT_R3, LDM_LT_R3);
        } else {
            if ((isRight && displayState.isRightDoorOpn) || (!isRight && displayState.isLeftDoorOpn)) {//本侧开门
                g.setColor(LDM_MAP_INFO.lightColor_DoorOpn);
                g.fillRect(LDM_Info.door_xs2 - LDM_LT_R3 / 2, LDM_Info.door_ys - LDM_LT_R3 / 2, LDM_LT_R3, LDM_LT_R3);
                g.setColor(LDM_MAP_INFO.LTBackgroundColor);
                g.fillRect(LDM_Info.door_xs1 - LDM_LT_R3 / 2, LDM_Info.door_ys - LDM_LT_R3 / 2, LDM_LT_R3, LDM_LT_R3);
            } else {//对侧开门
                g.setColor(LDM_MAP_INFO.lightColor_DoorCls);
                g.fillRect(LDM_Info.door_xs1 - LDM_LT_R3 / 2, LDM_Info.door_ys - LDM_LT_R3 / 2, LDM_LT_R3, LDM_LT_R3);
                g.setColor(LDM_MAP_INFO.LTBackgroundColor);
                g.fillRect(LDM_Info.door_xs2 - LDM_LT_R3 / 2, LDM_Info.door_ys - LDM_LT_R3 / 2, LDM_LT_R3, LDM_LT_R3);
            }
        }
    }
    if (LDM_Side_W != 0) {
        let LDM_LT_Up_H = LDM_Up_H / LDM_AF;
        let LDM_LT_Side_W = LDM_Side_W / LDM_AF;
        let LDM_LT_Stoke = LDM_LT_Side_W * 0.05;
        if (LDM_DoorState.stateNow() != "off" && ((isRight && displayState.isRightDoorOpn) || (!isRight && displayState.isLeftDoorOpn))) {
            g.setColor(LDM_MAP_INFO.DoorArrowOnColor);
        } else {
            g.setColor(LDM_MAP_INFO.DoorArrowOffColor);
        }
        //g.fillRect(LDM_LT_Stoke, LDM_LT_Up_H + LDM_LT_Stoke, LDM_LT_Side_W - LDM_LT_Stoke, LDM_LT_HEIGHT - LDM_LT_Up_H - 2 * LDM_LT_Stoke);
        g.fillRect(LDM_LT_WIDTH - LDM_LT_Side_W, LDM_LT_Up_H + LDM_LT_Stoke, LDM_LT_Side_W - LDM_LT_Stoke, LDM_LT_HEIGHT - LDM_LT_Up_H - 2 * LDM_LT_Stoke);
    }
}

// 清除圆形区域
function clipCircular(g, x, y, r) {
    const originalComposite = g.getComposite(); // 保存原始合成规则
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR)); // 设置为透明模式
    g.fillOval(x - r / 2, y - r / 2, r, r); // 填充圆形，使该区域变为透明
    g.setComposite(originalComposite); // 恢复原来的合成规则
}

/**
 * 绘制单个换乘箭头（圆弧）。
 * @param cx 中心x坐标
 * @param cy 中心y坐标
 * @param width 外椭圆长轴长
 * @param height 中心至箭头上顶点高度x2
 * @param angle 箭头角度，0为正上，正值为逆时针，负值为顺时针
 * @param arrowColor 箭头颜色
 */
function drawArrowOnCircleNN(g, cx, cy, width, height, angle, arrowColor) {
    //创建临时贴图
    let ARTex = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    let ga = ARTex.createGraphics();
    ga.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//启用抗锯齿

    //绘制外椭圆
    let centerX = width / 2, centerY = height / 2;
    let C1_r2 = height * 0.77;//外椭圆短轴长
    let C2_r2 = height * 0.59;//内椭圆短轴长
    let cdr = (C1_r2 - C2_r2) / 2;//内外椭圆半短轴长差，即箭头尾根宽度
    let C1_r1 = width - cdr;//外椭圆长轴长
    let C2_r1 = C1_r1 - cdr;//内椭圆长轴长
    ga.setColor(arrowColor);
    ga.fillOval(0, centerY - C1_r2 / 2, C1_r1, C1_r2);

    //绘制遮盖
    let originalComposite = ga.getComposite(); // 保存原始合成规则
    ga.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR)); // 设置为透明模式
    ga.fillOval(0, centerY - C2_r2 / 2, C2_r1, C2_r2); // 填充内椭圆，使该区域变为透明
    ga.fillRect(centerX, centerY - height / 2, width, height);
    ga.fillRect(centerX - width / 2, centerY, width, height);
    ga.setComposite(originalComposite); // 恢复原来的合成规则

    //绘制箭头部分
    let m = (height - 6 * cdr) / 4;
    let s = (height - 3 * cdr) / 4;
    let triangle = new Polygon();
    triangle.addPoint(centerX, centerY - cdr);//(0,r)
    triangle.addPoint(centerX + s, centerY - s - cdr);//(s,s+r)
    let xl = centerX - cdr / 2;//-r/2
    triangle.addPoint(xl, 0);//(-r/2, 1)
    let yu = centerY - 1.5 * cdr;//3/2r
    triangle.addPoint(xl, yu - 2 * m);//(-r/2, 3/2r+2m)
    triangle.addPoint(centerX, yu - 2 * m);//(0, 3/2r+2m)
    triangle.addPoint(centerX + m, yu - m);//(m, 3/2r+m)
    triangle.addPoint(centerX, yu);//(0, 3/2r)
    ga.fillPolygon(triangle);

    //将临时贴图绘制到画板
    let defaultAt = g.getTransform();
    let at = new AffineTransform();
    at.setToRotation((angle + 45) * Math.PI / 180, cx, cy);
    g.transform(at);//旋转画板
    g.drawImage(ARTex, cx - width / 2, cy - height / 2, width, height, null);//绘制贴图
    g.setTransform(defaultAt);//恢复变换

    ga.dispose();
    ARTex = null;//丢掉临时贴图
}

/**绘制换乘线路名 */
function drawLDMInterChangeName(g, xl, yl, width, height, routeName, routeColor) {
    //绘制线路色外边框
    let minA = Math.min(width, height);
    let arcW = 0.2 * minA;
    g.setColor(routeColor);
    g.fillRoundRect(xl, yl, width, height, arcW, arcW);

    //原色覆盖
    let stoke = Math.ceil(0.05 * minA);
    let xil = xl + stoke, yil = yl + stoke, wil = width - 2 * stoke, hi = height - 2 * stoke;
    arcW -= stoke * 2;
    g.setColor(LDM_MAP_INFO.backgroundColor);
    g.fillRoundRect(xil, yil, wil, hi, arcW, arcW);

    //拆分线路名文本
    let routeName_CJK = TextUtil.getCjkParts(routeName) + "";
    let routeName_NonCJK = TextUtil.getNonCjkParts(routeName) + "";

    //如果双语则深色下半
    let yc = yl + height / 2, hh = height / 2 - stoke;
    if (routeName_CJK && routeName_NonCJK) {
        let darkColor = lightenColor(routeColor, -0.7);//深色
        g.setColor(darkColor);
        g.fillRoundRect(xil, yc, wil, hh, arcW, arcW);
        g.fillRect(xil, yc, wil, height / 4);

        drawSingleLineText(g, routeName_CJK, xil, yil, wil, hh, Color.BLACK, true, LDM_FONT_CJK, 0);
        drawSingleLineText(g, routeName_NonCJK, xil, yc, wil, hh, Color.WHITE, true, LDM_FONT_NONCJK, 0);
    } else if (!!routeName_CJK) {
        drawSingleLineText(g, routeName_CJK, xil, yil, wil, hi, Color.BLACK, true, LDM_FONT_CJK, 0);
    } else {
        drawSingleLineText(g, routeName_NonCJK, xil, yil, wil, hi, Color.BLACK, true, LDM_FONT_NONCJK, 0);
    }
}

/**
 * 将给定的颜色变亮。
 * 将每个颜色分量向 255 靠近，靠近的程度由 factor 决定。factor 的值应该在 [0, 1] 范围内。如果 factor 为 0，则颜色不会变亮；如果 factor 为 1，则颜色将完全变亮，变为白色。
 * @param {Color} color 
 * @param {Number} factor 
 * @return {Color} Java 的 Color 对象。
 */
function lightenColor(color, factor) {
    // factor > 0 变亮，factor < 0 变暗，-1 ≤ factor ≤ 1 安全
    let r = color.getRed();
    let g = color.getGreen();
    let b = color.getBlue();

    let targetR, targetG, targetB;

    if (factor >= 0) {
        targetR = 255;
        targetG = 255;
        targetB = 255;
    } else {
        targetR = 0;
        targetG = 0;
        targetB = 0;
        factor = -factor;
    }

    let newR = r + (targetR - r) * factor;
    let newG = g + (targetG - g) * factor;
    let newB = b + (targetB - b) * factor;

    return rgbToColor(
        Math.min(255, Math.max(0, Math.round(newR))),
        Math.min(255, Math.max(0, Math.round(newG))),
        Math.min(255, Math.max(0, Math.round(newB)))
    );
}

//绘制大灯泡外圆环
function drawLDMStnCircle(g, xs, ys, r1, r2, color) {
    g.setColor(color);
    g.fillOval(xs - r1 / 2, ys - r1 / 2, r1, r1);
    clipCircular(g, xs, ys, r2);
}

//遍历并列出所有站点的换乘信息
function listStationInterchange_dl(stationInfoList, station_Total) {
    let interchangePart = {};
    interchangePart.MTconst = 0;//多线换乘计数

    for (let i = 0; i < station_Total; i++) {
        let interchangeInfo = stationInfoList[i].interchangeInfo;
        interchangePart[i] = {};
        interchangePart[i].type = 0;//没有换乘:0,单线换乘:1,多线换乘:2
        interchangePart[i].interchangeDetails = [];
        if (interchangeInfo && interchangeInfo.length > 0) {
            for (let j = 0; j < interchangeInfo.length; j++) {
                let routeName = interchangeInfo[j].name;
                let routeName_CJK = TextUtil.getCjkParts(routeName) + "";
                let routeName_NonCJK = TextUtil.getNonCjkParts(routeName) + "";

                //根据类型储存换乘信息
                interchangePart[i].interchangeDetails[j] = {}; // 初始化换乘信息对象
                interchangePart[i].interchangeDetails[j].color = interchangeInfo[j].color;
                interchangePart[i].interchangeDetails[j].shouldDL = routeName_CJK && routeName_NonCJK;//是否双行绘制中英文换乘线路名
                interchangePart[i].interchangeDetails[j].isCjk = CheckCJK(routeName);
                interchangePart[i].interchangeDetails[j].routeName_Cjk = routeName_CJK;
                interchangePart[i].interchangeDetails[j].routeName_NonCjk = routeName_NonCJK;
            }
            if (interchangeInfo.length == 1) {//如果只有1个换乘信息
                interchangePart[i].type = 1;
            } else if (interchangeInfo.length > 1) {//多个换乘信息
                interchangePart[i].type = 2;
                interchangePart.MTconst++;
            }
        }
    }
    return interchangePart;
}

//清屏用
function drawLDMLTBG(g, width, height, color) {
    g.setColor(color);
    g.fillRect(0, 0, width, height);
}

/**
 * 绘制蓝屏信息。
 * @param {Graphics2D} g
 * @param {string} str - 要显示的提示信息（可选）
 * @param {Color} baseColor - 背景色
 * @param {Color} strColor - 字体色
 */
function drawBlueScreen(g, str, baseColor, strColor) {//绘制蓝屏信息
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//启用抗锯齿
    //背景
    g.setColor(baseColor);
    g.fillRect(0, 0, LDM_WIDTH, LDM_HEIGHT);

    //画作者
    let font = LDM_FONT_CJK.deriveFont(12.0);
    let fm = g.getFontMetrics(font);
    g.setColor(strColor);
    g.setFont(font);
    g.drawString(WH_AUTHOR, LDM_WIDTH - fm.stringWidth(WH_AUTHOR) - 10, LDM_HEIGHT - fm.getHeight());

    //绘制提示信息
    if (!!str) {
        let height = 0.5 * LDM_HEIGHT;
        let y = (LDM_HEIGHT - height) / 2;
        drawSingleLineText(g, getMatching(str, true), 0, y, LDM_WIDTH, height * 0.7, strColor, true, LDM_FONT_CJK, 0);
        drawSingleLineText(g, getMatching(str, false), 0, y + height * 0.7, LDM_WIDTH, height * 0.3, strColor, true, LDM_FONT_NONCJK, 0);
    }
}

//检查是否双语
function CheckDL(stationInfoList) {
    for (let i = 0; i < stationInfoList.length; i++) {//循环站名
        if (stationInfoList[i] && stationInfoList[i].stationName.includes("|")) {//如果 stationInfoList.str 包含"|"则返回true
            return true;
        }
    }
    return false;
}

//绘制带有描边的箭头
function drawStrokeArrow(g, centerX, centerY, angle, StrokeColor, arrowColor, length, width, StrokeWidth) {
    //旋转变换
    let defaultAt = g.getTransform();
    let at = new AffineTransform();
    at.setToRotation(angle * Math.PI / 180, centerX, centerY);
    g.transform(at);

    //绘制描边
    if (StrokeColor) {
        g.setColor(StrokeColor);
        // 使用 Polygon 绘制箭头
        let triangle = new Polygon();
        triangle.addPoint(centerX, centerY - width / 2);
        triangle.addPoint(centerX + length / 2, centerY);
        triangle.addPoint(centerX, centerY + width / 2);
        triangle.addPoint(centerX, centerY + width / 4);
        triangle.addPoint(centerX - length / 2, centerY + width / 4);
        triangle.addPoint(centerX - length / 2, centerY - width / 4);
        triangle.addPoint(centerX, centerY - width / 4);
        g.fillPolygon(triangle);
    }

    //绘制箭头，如果 arrowColor 为 null 则设置为擦除模式
    let originalComposite = g.getComposite(); // 保存原始合成规则
    if (!arrowColor) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR)); // 设置为透明模式
    } else {
        g.setColor(arrowColor);
    }

    // 使用 Polygon 绘制三角形
    let TanArrowAngle = width / length;//箭头半夹角tan值
    let SinArrowAngle = (width / 2) / Math.sqrt(Math.pow(width / 2, 2) + Math.pow(length / 2, 2));//箭头半夹角sin值
    let CosArrowAngle = SinArrowAngle / TanArrowAngle;
    let sy = StrokeWidth / CosArrowAngle + StrokeWidth * TanArrowAngle;
    let sx = StrokeWidth / SinArrowAngle;
    let triangle = new Polygon();
    triangle.addPoint(centerX + StrokeWidth, centerY - width / 2 + sy);
    triangle.addPoint(centerX + length / 2 - sx, centerY);
    triangle.addPoint(centerX + StrokeWidth, centerY + width / 2 - sy);
    triangle.addPoint(centerX + StrokeWidth, centerY + width / 4 - StrokeWidth);
    triangle.addPoint(centerX - length / 2 + StrokeWidth, centerY + width / 4 - StrokeWidth);
    triangle.addPoint(centerX - length / 2 + StrokeWidth, centerY - width / 4 + StrokeWidth);
    triangle.addPoint(centerX + StrokeWidth, centerY - width / 4 + StrokeWidth);
    g.fillPolygon(triangle);

    //恢复变换和原来的合成规则
    g.setTransform(defaultAt);
    g.setComposite(originalComposite);
}

//检查两侧车门打开情况
function checkDoorhasOpn(train) {
    let isLeftDoorOpn = false;
    let isRightDoorOpn = false;
    for (let i = 0; i < train.trainCars(); i++) {
        isLeftDoorOpn = isLeftDoorOpn || train.doorLeftOpen[i];
        isRightDoorOpn = isRightDoorOpn || train.doorRightOpen[i];
    }
    return { isLeftDoorOpn, isRightDoorOpn };
}