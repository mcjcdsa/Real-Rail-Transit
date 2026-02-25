include(Resources.id("mtr:kmjs/wh_util.js"));

//资源载入
let rawDL12DoorLightOnModel = ModelManager.loadRawModel(Resources.manager(), Resources.idr("doorlight.obj"), null);//导入obj模型
//let rawDL12DoorLightRedModel = ModelManager.loadRawModel(Resources.manager(), Resources.idr("doorlight_red.obj"), null);//导入obj模型
rawDL12DoorLightOnModel.applyUVMirror(false, true);//UV翻转
//rawDL12DoorLightRedModel.applyUVMirror(false, true);//UV翻转
rawDL12DoorLightOnModel.setAllRenderType("light");//设置渲染类型
//rawDL12DoorLightRedModel.setAllRenderType("light");//设置渲染类型
var DL12DoorLightOnModel = ModelManager.uploadVertArrays(rawDL12DoorLightOnModel);//上传模型
//var DL12DoorLightRedModel = ModelManager.uploadVertArrays(rawDL12DoorLightRedModel);//上传模型

const DoorLight_RateLimit = 0.5;//门灯闪烁频率，默认0.5s

function create(ctx, state, train) {
    //初始化设置
    state.DoorLight_State = new StateTracker();
    state.DoorLight_State.setState(train.doorValue() == 1 ? "On" : "Off");
    state.DoorOpnInfo = { isLeftDoorOpn: false, isRightDoorOpn: false };
}

function render(ctx, state, train) {
    //更新门灯状态
    if ((train.doorValue() > 0 && state.lastDoorValue_ForDoorlight == 0)) {
        state.DoorLight_State.setState("On");
        //print("state.DoorLight_State.stateNow():" + state.DoorLight_State.stateNow());
    } else if (state.DoorLight_State.stateNowDuration() >= DoorLight_RateLimit) {
        if (train.doorValue() > 0 && state.DoorLight_State.stateNow() == "Off") {
            state.DoorLight_State.setState("On");
            //print("state.DoorLight_State.stateNow():" + state.DoorLight_State.stateNow());
        } else if (train.doorValue() < 1 && state.DoorLight_State.stateNow() == "On" && train.doorValue() < state.lastDoorValue_ForDoorlight) {
            state.DoorLight_State.setState("Off");
            //print("state.DoorLight_State.stateNow():" + state.DoorLight_State.stateNow());
        }
    }

    if (train.doorValue() != state.lastDoorValue_ForDoorlight) {
        if (train.doorValue() > 0 && !state.lastDoorValue_ForDoorlight) {
            state.DoorOpnInfo = checkDoorhasOpn(train);
        } else if (train.doorValue() == 0) {
            state.DoorOpnInfo = { isLeftDoorOpn: false, isRightDoorOpn: false };
        }
        state.lastDoorValue_ForDoorlight = train.doorValue();
    }

    let zOffset, zOffsetF;
    for (let i = 0; i < train.trainCars(); i++) {
        //设置位移
        if (i == 0) {//头车
            zOffset = [-0.265625];
            zOffsetF = [0];
        } else if (i == train.trainCars() - 1) {//尾车   
            zOffset = [0];
            zOffsetF = [0.265625];
        } else {//中间车
            zOffset = [-0.265625];
            zOffsetF = [0];
        }

        //绘制门灯
        if (train.doorLeftOpen[i]) {
            if (state.DoorLight_State.stateNow() == "On") {
                drawDoorLightModel(ctx, DL12DoorLightOnModel, i, null, zOffsetF);
            }
        } else if (state.DoorOpnInfo.isLeftDoorOpn && train.doorValue() > 0) {
            //drawDoorLightModel(ctx, DL12DoorLightRedModel, i, null, zOffsetF);
        }
        if (train.doorRightOpen[i]) {
            if (state.DoorLight_State.stateNow() == "On") {
                drawDoorLightModel(ctx, DL12DoorLightOnModel, i, zOffset, null);
            }
        } else if (state.DoorOpnInfo.isRightDoorOpn && train.doorValue() > 0) {
            //drawDoorLightModel(ctx, DL12DoorLightRedModel, i, zOffset, null);
        }
    }
}

function drawDoorLightModel(ctx, Model, i, zOffset, zOffsetF) {
    let matrices = new Matrices(); // 引入恢复矩阵
    let k = zOffset ? zOffset.length : 0; // 如果zOffset不为空，获取其长度
    if (zOffset != null && k > 0) {
        matrices.pushPose();
        for (let j = 0; j < k; j++) {
            matrices.translate(0, 0, zOffset[j]); // 平移
            ctx.drawCarModel(Model, i, matrices); // 绘制车厢模型
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
            ctx.drawCarModel(Model, i, matrices); // 绘制车厢模型
            matrices.popPushPose();
        }
        matrices.popPose();
    }
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