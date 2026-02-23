importPackage(java.awt);

const WH_AUTHOR = "Made by lanoel";//作者名，由lanoel制作，改编自Jeffreyg1228的上海风格LCD、王下一桶cr200j的电视，参考zbx和Aphrodite的相关代码

//字体库
const SOURCE_HAN_SANS_CN_BOLD = Resources.readFont(Resources.id("mtr:kmjs/fonts/source-han-sans-cn/source-han-sans-cn-bold.otf"));//黑体粗
//const XIAWU = Resources.readFont(Resources.id("mtr:kmjs/fonts/xiawu/xiawu.ttf"));//无衬线黑体
const SANS_LAO = Resources.readFont(Resources.id("mtr:kmjs/fonts/sans_lao/sans_lao.ttf"));//英文
//const GUANGKEKAI = Resources.readFont(Resources.id("mtr:kmjs/fonts/guangkekai/guangkekai.ttf"));//楷体
const UNIFONT_16PX = Resources.readFont(Resources.id("mtr:kmjs/fonts/unifont/unifont_16px.ttf"));//点阵

//翻转矩阵(绕Y轴旋转180°)
//const matricesRY = new Matrices();//一个绕Y轴旋转180°的矩阵
//matricesRY.setIdentity();//初始化
//matricesRY.rotateY(Math.PI);

//列车状态( 移植自 Jeffreyg1228 的上海风格 LCD )
/**
 * 表示列车无有效路线。
 */
const STATUS_NO_ROUTE = "no_route";
/**
 * 表示列车正在车厂中等待发车。
 */
const STATUS_WAITING_FOR_DEPARTURE = "waiting_for_departure";
/**
 * 表示列车正在离开车厂。
 */
const STATUS_LEAVING_DEPOT = "leaving_depot";
/**
 * 表示列车正在路线区间正常行驶。
 */
const STATUS_ON_ROUTE = "on_route";
/**
 * 表示列车正停靠站台。
 */
const STATUS_ARRIVED = "arrived";
/**
 * 表示列车正由当前线路终点站驶向下一线路起点站。
 */
const STATUS_CHANGING_ROUTE = "changing_route";
/**
 * 表示列车正在返回车厂。
 */
const STATUS_RETURNING_TO_DEPOT = "returning_to_depot";