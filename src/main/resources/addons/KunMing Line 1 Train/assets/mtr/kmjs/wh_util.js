importPackage(java.awt);

//-----------------------------------文-本-类-函-数-----------------------------------
//计算横排字体大小，lanoel修改fix精简版
function calculateMaxFontSizeN(g, font, text, maxWidth, maxHeight, minFontSize, maxFontSize) {
    if (text == null || maxWidth < 0 || maxHeight < 0 || minFontSize < 0 || maxFontSize < 0 || (maxWidth == 0 && maxHeight == 0 && maxFontSize == 0)) {
        throw new Error("calculateMaxFontSizeN: Invalid input parameters. text: " + text + ", maxWidth: " + maxWidth + ", maxHeight: " + maxHeight + ", minFontSize: " + minFontSize + ", maxFontSize: " + maxFontSize);
    }
    let fontSize = minFontSize;
    let fontMetrics;
    do {
        fontSize += 0.5;
        fontMetrics = g.getFontMetrics(font.deriveFont(fontSize));
    } while ((maxWidth == 0 || fontMetrics.stringWidth(text) < maxWidth) && (maxHeight == 0 || fontMetrics.getHeight() < maxHeight));
    return maxFontSize == 0 ? fontSize : Math.min(fontSize, maxFontSize);
}

//计算横排或竖排字体大小，lanoel修改pro max版
function calculateMaxFontSizeV(g, font, text, maxWidth, maxHeight, minFontSize, maxFontSize, vertical, isCjk) {
    if (text == null || maxWidth < 0 || maxHeight < 0 || minFontSize < 0 || maxFontSize < 0 || (maxWidth == 0 && maxHeight == 0 && maxFontSize == 0)) {
        throw new Error("calculateMaxFontSizeV: Invalid input parameters. text: " + text + ", maxWidth: " + maxWidth + ", maxHeight: " + maxHeight + ", minFontSize: " + minFontSize + ", maxFontSize: " + maxFontSize);
    }
    let fontSize = minFontSize;
    let fontMetrics;
    do {
        fontSize += 0.5;
        fontMetrics = g.getFontMetrics(font.deriveFont(fontSize));
    } while (
        (vertical ? 
            (isCjk ? 
                maxWidth == 0 || fontMetrics.charWidth('龘') < maxWidth : 
                maxWidth == 0 || fontMetrics.getHeight() < maxWidth) : 
            maxWidth == 0 || fontMetrics.stringWidth(text) < maxWidth
        ) 
        && 
        (vertical ? 
            (isCjk ? 
                maxHeight == 0 || (fontMetrics.getAscent() * 0.8 + fontMetrics.getDescent() * 0.5) * text.length() - 0.2*fontMetrics.getAscent() - 0.5*fontMetrics.getDescent() < maxHeight : 
                maxHeight == 0 || fontMetrics.stringWidth(text) < maxHeight) : 
            maxHeight == 0 || fontMetrics.getHeight() < maxHeight)
    );
    return maxFontSize == 0 ? fontSize : Math.min(fontSize, maxFontSize);
}

//检查文本是否含有CJK部分
function CheckCJK(text) {
    if (TextUtil.getCjkParts(text).isEmpty()) {
        return false;
    } else {
        return true;
    }
}

/**
 * 在用“||”分割的字符串中，返回“||”之前的所有字符。
 * 如果源字符串不使用“||”分割，则返回该字符串。
 * @param {String} src 源字符串。
 * @returns {String}
 */
function getNonExtraParts(src) {
    return src.includes("||") ? TextUtil.getNonExtraParts(src) : src;
}

/**
 * 在用“|”分割的字符串中，获取其中的 CJK / 非 CJK 部分。
 * 如果源字符串不使用“|”分割，但使用“||”分割，则返回“||”之前的所有字符。
 * 如果源字符串既不使用“|”分割，也不使用“||”分割，则返回该字符串。
 * 如果源字符串多次使用“|”分割，则判断每个部分是否为 CJK 字符，并返回符合条件的所有部分。每个部分间用空格分割。
 * @param {String} src 源字符串。
 * @param {Boolean} isCjk 指定获取字符串中的 CJK 还是非 CJK 部分。
 * @returns {String}
 */
function getMatching(src, isCjk) {
    if (!src.includes("|")) {
        return getNonExtraParts(src);
    }
    return isCjk ? TextUtil.getCjkParts(src) : TextUtil.getNonCjkParts(src);
}

/**
 * 在给定区域内绘制横排单行文本。
 * @param g Graphics2D 对象，用于绘制文本。
 * @param str 要绘制的文本字符串。
 * @param x 区域左顶点的 x 坐标。
 * @param y 区域左顶点的 y 坐标。
 * @param length 区域的宽度。
 * @param height 区域的高度。
 * @param color 文本颜色。
 * @param isCenter 是否居中对齐文本。如果为 true，则文本将在区域内水平居中对齐。
 * @param fontToDraw 使用的字体。
 * @param uniformFontSize 限制字体大小。传入0则自适应。
 */
function drawSingleLineText(g, str, x, y, length, height, color, isCenter, fontToDraw, uniformFontSize) {
    let fontSize = uniformFontSize ? uniformFontSize : calculateMaxFontSizeN(g, fontToDraw, str, length, height, 0, 0);
    let font = fontToDraw.deriveFont(fontSize);
    let fm = g.getFontMetrics(font);
    g.setFont(font);
    g.setColor(color);
    y += (height - fm.getHeight()) / 2 + fm.getAscent(); // 垂直居中
    x += isCenter ? (length - fm.stringWidth(str)) / 2 : 0; // 水平居中或左对齐
    g.drawString(str, x, y);
}

/**
 * 绘制长文本，自动调整字体大小并换行
 * @param {Graphics2D} g - 绘图上下文对象，用于绘制文本
 * @param {string} str - 要绘制的文本内容
 * @param {number} x - 文本框左上 x 坐标
 * @param {number} y - 文本框左上 y 坐标
 * @param {number} width - 文本框宽度
 * @param {number} height - 文本框高度
 * @param {Font} fontToDraw - 使用字体
 * @param {number} minFontSize - 字体大小的最小值
 * @param {number} maxFontSize - 字体大小的最大值
 * @param {boolean} isCjk - 是否是CJK(影响分割符使用，CJK采用"、"，非CJK采用",")
 */
function drawLongTextAuto(g, str, x, y, width, height, fontToDraw, minFontSize, maxFontSize, isCjk) {
    // 初始化字体大小
    let fontSize = Math.max(calculateMaxFontSizeN(g, fontToDraw, str, width, height, minFontSize + 0.5, maxFontSize) - 0.5, 0);//计算函数总会超一点点
    let font = fontToDraw.deriveFont(fontSize);
    let fm = g.getFontMetrics(font);
    let currentWidth = fm.stringWidth(str);
    let multiplier = 1;//行数
    let lineHeight = fm.getAscent(); // 收窄行间距
    let descent = (height - fm.getHeight())/2 + fm.getAscent();
    //print("pre_fontSize:" + fontSize + ", currentWidth:" + currentWidth + ", width:" + width + ", height:" + height + ", minFontSize:" + minFontSize + ", maxFontSize:" + maxFontSize);

    // 动态调整字体大小
    while ((currentWidth > width * multiplier || fm.getHeight() < height/multiplier/2) && fontSize > minFontSize) {// 按照规则逐步减小字体大小
        multiplier++;
        fontSize = Math.max(calculateMaxFontSizeN(g, fontToDraw, str, 0, height/multiplier, minFontSize, maxFontSize) - 0.5, 0);
        font = fontToDraw.deriveFont(fontSize);
        fm = g.getFontMetrics(font);
        currentWidth = fm.stringWidth(str);
        lineHeight = fm.getAscent(); // 收窄行间距
        descent = (height - multiplier*lineHeight)/2 + fm.getAscent()*0.75;
    }
    //print("final_fontSize:" + fontSize + ", multiplier:" + multiplier);

    // 初始化变量
    g.setFont(font);
    let totalHeight = 0;
    let currentLine = "";
    let words = isCjk ? str.split("、") : str.split(","); // 根据 isCjk 决定分割符
    let wordIndex = 0;

    // 绘制文本，直到整个段落都被绘制
    while (wordIndex < words.length && totalHeight < height) {
        let word = words[wordIndex];
        let testLine = currentLine + (currentLine.length > 0 ? (isCjk ? "、" : ",") : "") + word;

        // 检查当前行是否超出宽度
        if (fm.stringWidth(testLine) > width) {// 如果超出宽度，绘制当前行
            let lineX = x + (width - fm.stringWidth(currentLine)) / 2; // 居中对齐
            g.drawString(currentLine, lineX, y + totalHeight + descent);
            totalHeight += lineHeight;// 更新总高度
            currentLine = word;// 重置当前行
        } else {// 如果没有超出宽度，将当前词添加到当前行
            currentLine = testLine;
        }
        // 准备下一个词
        wordIndex++;
    }

    // 绘制最后一行
    if (currentLine.length > 0 && totalHeight < height) {
        let lineX = x + (width - fm.stringWidth(currentLine)) / 2; // 居中对齐
        g.drawString(currentLine, lineX, y + totalHeight + descent);
    }
}

/**
 * 绘制横排单行文本。
 * @param g 
 * @param str 需要绘制的文本
 * @param x 基点 x 坐标(一般为边线中点)
 * @param y 基点 y 坐标(一般为边线中点)
 * @param length 文本长度
 * @param height 文本高度
 * @param color 文本颜色
 * @param fontToDraw 文本使用字体
 * @param isCjk 文本是否Cjk，用于两端对齐绘制时的调用方式
 * @param uniformFontSize 限制字体大小，传入0则不限制
 * @param shouldJustified 文本是否两端对齐，true 为两端对齐，false 为居中对齐
 * @param alignedDirection 文本对齐方向("270"、"90"分别为上/下对齐，基点分别为上/下边线中点；"180"、"0"分别为左/右对齐，基点分别为左/右边线中点)
 * @param borderFactor 边框比例，比如 0.05 为保留 5% 边框位置
 * @param BG_Type 文字背景样式( null 为不绘制；"onlyColor"为纯色，采用 routeColor 填充背景；其他值为图片填充)
 * @param routeColor 文字背景颜色(可选)
 */
function drawHorizontalText(g, str, x, y, length, height, color, fontToDraw, isCjk, uniformFontSize, shouldJustified, alignedDirection, borderFactor, BG_Type, routeColor){
    //算参数
    let strDivideBorder = 1 - 2 * borderFactor;
    let fontSize = uniformFontSize ? uniformFontSize : calculateMaxFontSizeN(g, fontToDraw, str, length*strDivideBorder, height*strDivideBorder, 0, 0);//保留5%边距
    let font = fontToDraw.deriveFont(fontSize);
    let fm = g.getFontMetrics(font);
    let Bx = 0;//框坐标
    let By = 0;
    let Lx = 0;//基线坐标
    let Ly = 0;
    let strWidth = fm.stringWidth(str);
    let strHeight = 0.85 * fm.getAscent() + 0.6 * fm.getDescent();
    let boxWidth = strWidth/strDivideBorder;
    let boxHeight = Math.max(strHeight/strDivideBorder, height*0.67);
    if(shouldJustified){//两端对齐则调整框长度为全长
        strWidth = length * strDivideBorder;
        boxWidth = length;
    }

    //根据文本对齐方向设置绘制基点
    switch (alignedDirection % 360){
        case 0://右对齐
            Bx = x - boxWidth;
            By = y - boxHeight/2;
            Lx = x - (boxWidth + strWidth)/2;
            Ly = isCjk ? y + fm.getAscent()*0.31 : y - fm.getHeight() / 2 + fm.getAscent();//CJK和非CJK的居中量不同
            break;
        case 90://下对齐
            Bx = x - boxWidth/2;
            By = y - boxHeight;
            Lx = x - strWidth/2;
            Ly = y - Math.max((boxHeight - strHeight)/2, 0) - 0.05*fm.getHeight() - 0.5*fm.getDescent();
            break;
        case 180://左对齐
            Bx = x;
            By = y - boxHeight/2;
            Lx = x + (boxWidth - strWidth)/2;
            Ly = isCjk ? y + fm.getAscent()*0.31 : y - fm.getHeight() / 2 + fm.getAscent();//CJK和非CJK的居中量不同
            break;
        case 270://上对齐
            Bx = x - boxWidth/2;
            By = y;
            Lx = x - strWidth/2;
            Ly = y + Math.max((boxHeight - strHeight)/2, 0) + 0.05*fm.getHeight() + 0.75*fm.getAscent();
            break;
    }

    //绘制背景
    if(BG_Type != null){
        if(BG_Type === "onlyColor"){//纯色填充圆角矩形
            g.setColor(routeColor);
            g.fillRect(Bx, By, boxWidth, boxHeight);
        }else if(!!BG_Type){//图片填充
            try {
                g.drawImage(BG_Type, Bx, By, boxWidth, boxHeight, null);
            } catch (error) {// 提供默认背景或错误提示
                print("Failed to draw image background: BG_Type: " + BG_Type);
            }
        }
    }

    //绘制文本
    if(shouldJustified){//绘制两端对齐文本
        drawJustifiedTextH(g, str, Lx, Ly, strWidth, font, color, isCjk);
    }else{//绘制居中对齐文本
        g.setFont(font);
        g.setColor(color);
        g.drawString(str, Lx, Ly);
    }
}

/**
 * 绘制两端对齐的单行文本。
 * @param {Graphics2D} g
 * @param {string} text - 需要绘制的文本内容。
 * @param {number} x - 文本的起始 x 坐标。
 * @param {number} y - 文本的起始 y 坐标（基线坐标）。
 * @param {number} width - 文本的可用宽度。
 * @param {Font} font - 文本的字体。
 * @param {Color} color - 文本的颜色。
 * @param {boolean} isCjk - 文本是否为 CJK 字符。
 */
function drawJustifiedTextH(g, text, x, y, width, font, color, isCjk) {
    g.setColor(color);
    g.setFont(font);
    let fm = g.getFontMetrics(font);

    if(isCjk){//Cjk的处理方法
        // 如果只有一个字，则居中绘制
        if (text.length() == 1) {
            g.drawString(text, x + (width - fm.charWidth(text)) / 2, y);
            return;
        }

        // 计算总字符宽度
        let totalCharWidth = 0;
        let text1 = ""+text;
        for (let i = 0; i < text.length(); i++) {
            totalCharWidth += fm.charWidth(text1[i]);
        }
    
        // 计算额外间距
        let extraSpace = (width - totalCharWidth) / (text.length() - 1);
    
        // 绘制文本
        let currentX = x;
        for (let i = 0; i < text.length(); i++) {
            let textToDraw = text1[i];
            let charWidth = fm.charWidth(textToDraw);
            g.drawString(textToDraw, currentX, y);
            currentX += charWidth + extraSpace;
        }
    }else{//非Cjk的处理方法
        // 如果只有一个单词，则居中绘制
        let words = text.split(' ');
        if (words.length == 1) {
            g.drawString(text, x + (width - fm.stringWidth(text)) / 2, y);
            return;
        }

        // 计算单词的总宽度和空格的数量
        let totalWordWidth = 0;
        for (let i = 0; i < words.length; i++) {
            totalWordWidth += fm.stringWidth(words[i]);
        }
    
        // 计算额外的空格宽度
        let extraSpace = (width - totalWordWidth) / (words.length - 1);
    
        // 绘制文本
        let currentX = x;
        for (let i = 0; i < words.length; i++) {
            if (i > 0) {
                // 绘制额外的空格
                g.drawString(' ', currentX, y);
                currentX += extraSpace;
            }
            // 绘制单词
            g.drawString(words[i], currentX, y);
            currentX += fm.stringWidth(words[i]);
        }
    }
}

/**
 * 绘制上下对齐的垂直文本。
 * @param {Graphics2D} g
 * @param {string} str - 需要绘制的文本内容。
 * @param {number} x - 文本的基线中点 x 坐标。
 * @param {number} y - 文本的基线中点 y 坐标。
 * @param {number} width - 文本的可用宽度。
 * @param {number} height - 文本的可用高度。
 * @param {Color} color - 文本的颜色。
 * @param {Font} fontToDraw - 文本的字体。
 * @param {boolean} isCjk - 表示文本是否为 CJK（中日韩）字符。
 * @param {number} uniformFontSize - 限制字体大小，传入 0 时不限制。
 * @param {number} alignedDirection - 文本的对齐方向（90、270分别为下、上对齐）。
 * @param {number} borderFactor - 边距比例。
 * @param {string|null} BG_Type - 文字背景样式（null 为不绘制；"onlyColor" 为纯色，其他值为图片填充）。
 * @param {Color} routeColor - 文字背景颜色。
 */
function drawVerticalText(g, str, x, y, width, height, color, fontToDraw, isCjk, uniformFontSize, alignedDirection, borderFactor, BG_Type, routeColor){//x、y是基线中点
    // 计算参数
    let strDivideBorder = 1 - 2 * borderFactor;
    let fontSize = uniformFontSize ? uniformFontSize : calculateMaxFontSizeV(g, fontToDraw, str, width*strDivideBorder, height*strDivideBorder, 0, 0, true, isCjk);
    let font = fontToDraw.deriveFont(fontSize);
    let fm = g.getFontMetrics(font);
    let By = 0;//边框位置
    let Ly = 0;//字体基线位置
    let strWidth = 0;
    if(isCjk){
        strWidth = fm.charWidth("龘");
    }else{
        strWidth = fm.getAscent()*0.8 + fm.getDescent();
    }
    let strHeight = height * strDivideBorder;
    let boxWidth = Math.max(strWidth/strDivideBorder, width*0.67);
    let boxHeight = height;
    let Bx = x - boxWidth/2;

    switch (alignedDirection % 360){
        case 90://下对齐
            By = y - boxHeight;
            Ly = y - height * (1 - borderFactor);
            break;
        case 270://上对齐
            By = y;
            Ly = y + height * borderFactor;
            break;
    }

    //是否绘制背景
    if(BG_Type != null){
        if(BG_Type === "onlyColor"){//纯色填充圆角矩形
            g.setColor(routeColor);
            g.fillRect(Bx, By, boxWidth, boxHeight);
        }else if(!!BG_Type){//图片填充
            try {
                //旋转变换
                let defaultAt = g.getTransform();
                let at = new AffineTransform();
                at.setToRotation(Math.PI / 2, Bx, By);
                g.transform(at);

                //绘制图片
                g.drawImage(BG_Type, Bx, By - boxWidth, boxHeight, boxWidth, null);

                g.setTransform(defaultAt);
            } catch (error) {// 提供默认背景或错误提示
                print("Failed to draw image background: BG_Type: " + BG_Type);
            }
        }
    }

    //绘制字体内容
    if(isCjk){
        drawJustifiedVerticalTextCJK(g, str, x, Ly, strHeight, color, font);//上下两端对齐的竖直文本绘制
    }else{
        drawJustifiedVerticalTextNonCJK(g, str, x, Ly, strHeight, color, font);//上下两端对齐的竖直文本绘制，但旋转90°
    }
}

/**
 * 绘制顺时针旋转90°上下对齐的竖排单行非CJK文本。
 * @param centerX 中心x
 * @param topY 下对齐底线
 * @param height 内容高度限制
 */
function drawJustifiedVerticalTextNonCJK(g, strToDraw, centerX, topY, height, color, font){
    g.setColor(color);
    g.setFont(font);
    let fm = g.getFontMetrics(font);

    //旋转变换
    let defaultAt = g.getTransform();
    let at = new AffineTransform();
    at.setToRotation(Math.PI / 2, centerX, topY);
    g.transform(at);
    let rotationCenterX = centerX + fm.getDescent() * 0.75 - fm.stringWidth("A");
    let rotationCenterY = topY - fm.getHeight() / 2 + fm.getAscent();

    // 如果只有一个单词，则居中绘制
    let words = strToDraw.split(' ');
    if (words.length === 1) {
        let wordWidth = fm.stringWidth(words[0]);
        g.drawString(strToDraw, rotationCenterX + (height - wordWidth) / 2, rotationCenterY);
        g.setTransform(defaultAt);
        return;
    }

    // 计算单词的总宽度和空格的数量
    let totalWordWidth = 0;
    for (let i = 0; i < words.length; i++) {
        totalWordWidth += fm.stringWidth(words[i]);
    }

    // 计算额外的空格宽度
    let extraSpace = (height - totalWordWidth) / (words.length - 1);

    // 绘制文本
    let currentX = rotationCenterX;
    for (let i = 0; i < words.length; i++) {
        if (i > 0) {
            // 绘制额外的空格
            g.drawString(' ', currentX, rotationCenterY);
            currentX += extraSpace;
        }
        // 绘制单词
        g.drawString(words[i], currentX, rotationCenterY);
        currentX += fm.stringWidth(words[i]);
    }
    g.setTransform(defaultAt);
}

/**
 * 绘制上下对齐的竖排单行文本。
 * @param centerX 中心x
 * @param topY 上对齐底线
 * @param height 内容高度限制
 */
function drawJustifiedVerticalTextCJK(g, strToDraw, centerX, topY, height, color, font){
    g.setColor(color);
    g.setFont(font);
    let fm = g.getFontMetrics(font);
    let charHeight = fm.getHeight();// 获取单个字符的高度

    // 如果字符串长度为1，单独处理
    if (strToDraw.length() == 1) {
        g.drawString(strToDraw, centerX - fm.charWidth(strToDraw)*0.53, topY + (height + charHeight)/2 - charHeight);//居中绘制单字
        return;
    }

    // 遍历每个字符并绘制
    let spaceBetweenChars = (height + fm.getAscent()/4 + fm.getDescent()/2 - charHeight * strToDraw.length()) / (strToDraw.length() - 1);// 计算额外的间距（用于上下两端对齐）
    let str = "" + strToDraw;
    for (let i = 0; i < strToDraw.length(); i++) {
        let char = str[i];
        let currentY = topY + 0.7*fm.getAscent() + (charHeight + spaceBetweenChars) * i;// 计算当前字符的垂直位置
        g.drawString(char, centerX - fm.charWidth(char)*0.53, currentY);// 绘制字符，稍微偏左补偿
    }
}

//-----------------------------------图-片-类-函-数-----------------------------------
/**
 * 统计指定文件夹中特定扩展名的图片数量
 * @param {string} folderPath - 图片文件所在的文件夹路径
 * @param {string} extension - 图片文件的扩展名（例如 ".png" 或 ".jpg"）
 * @returns {number} - 文件夹中符合条件的图片总数
 */
function countImagesInFolder(folderPath, extension) {
    let imageCount = 0; // 初始化图片计数器
    let currentNumber = 1; // 从编号1开始尝试访问图片

    // 尝试不断读取图片，直到找不到为止
    while (true) {
        let imagePath = folderPath + currentNumber + extension;// 构造图片路径
        try {// 尝试读取图片
            let image = Resources.readBufferedImage(Resources.id(imagePath));
            if (image) {
                imageCount++;// 如果图片存在，计数器加1
            } else {
                break;// 如果图片不存在，跳出循环
            }
        } catch (error) {
            break;// 如果读取失败（例如文件不存在），跳出循环
        }
        // 尝试下一个编号
        currentNumber++;
    }
    return imageCount;
}

/**
 * 绘制区域渐变
 * @param g Graphics2D 对象，用于绘制
 * @param startColor 渐变起始颜色
 * @param endColor 渐变结束颜色
 * @param x 渐变矩形的起始 x 坐标
 * @param y 渐变矩形的起始 y 坐标
 * @param width 渐变矩形的宽度
 * @param height 渐变矩形的高度
 * @param direction 渐变方向，0 表示从上到下，1 表示从左到右
 */
function drawGradient(g, startColor, endColor, x, y, width, height, direction) {
    let fractions = [0.0, 1.0]; // 渐变位置
    let colors = [startColor, endColor]; // 渐变颜色

    // 根据方向创建渐变
    let gradient;
    if (direction === 0) { // 从上到下
        gradient = new LinearGradientPaint(
            x, y, // 渐变起点坐标
            x, y + height, // 渐变终点坐标
            fractions, // 渐变位置
            colors // 渐变颜色
        );
    } else { // 从左到右
        gradient = new LinearGradientPaint(
            x, y, // 渐变起点坐标
            x + width, y, // 渐变终点坐标
            fractions, // 渐变位置
            colors // 渐变颜色
        );
    }

    // 设置渐变并绘制矩形
    g.setPaint(gradient);
    g.fillRect(x, y, width, height);
}

/**
 * 绘制行车方向箭头。
 * @param centerX 中心x坐标
 * @param centerY 中心y坐标
 * @param angle 箭头角度，0为3点钟方向，正值为顺时针，单位为角度
 * @param arrowColor 箭头颜色
 * @param length 箭头长度
 * @param width 箭头宽度
 */
function drawArrow(g, centerX, centerY, angle, arrowColor, length, width) {
    // 设置线条颜色和宽度
    g.setColor(arrowColor);
    let lineWidth = 0.5 * width;//线宽
    let lineLength = 0.7 * length;//缩减线长，给箭头留空间

    //旋转变换
    let defaultAt = g.getTransform();
    let at = new AffineTransform();
    at.setToRotation(angle * Math.PI / 180, centerX, centerY);
    g.transform(at);

    //绘制线条
    g.fillRect(centerX - length / 2, centerY - lineWidth / 2, lineLength, lineWidth);

    // 使用 Polygon 绘制三角形
    let triangle = new Polygon();
    triangle.addPoint(centerX + length / 2, centerY);
    triangle.addPoint(centerX, centerY - width / 2);
    triangle.addPoint(centerX, centerY + width / 2);
    g.fillPolygon(triangle);

    //恢复变换
    g.setTransform(defaultAt);
}

//-----------------------------------颜-色-类-函-数-----------------------------------
//将 RGBA（红、绿、蓝、透明度）颜色值组合成一个 32 位整数。
function getRGBAValue(r, g, b, a) {
    return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF));
}

/**
 * 通过 R、G、B 值创建 Java 的 Color 对象。
 * 不直接 new 的原因是 JavaScript 中的数字在 Rhino 中总是映射为 Java 的 double 类型，这样匹配的 Color 对象的构造方法仅接受 0~1 范围内的数字。
 * @param {Number} r 
 * @param {Number} g 
 * @param {Number} b 
 * @return {Color} Java 的 Color 对象。
 */
function rgbToColor(r, g, b) {
    r = (r - 0.5) / 255;
    g = (g - 0.5) / 255;
    b = (b - 0.5) / 255;
    return new java.awt.Color(clamp(r, 0, 1), clamp(g, 0, 1), clamp(b, 0, 1));
}

//判断颜色是否是浅色
function isLightColor(color) {
    let darkness = 1 - (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
    return darkness < 0.5;
}

/**
 * 将给定的颜色变亮。
 * 将每个颜色分量向 255 靠近，靠近的程度由 factor 决定。factor 的值应该在 [0, 1] 范围内。如果 factor 为 0，则颜色不会变亮；如果 factor 为 1，则颜色将完全变亮，变为白色。
 * @param {Color} color 
 * @param {Number} factor 
 * @return {Color} Java 的 Color 对象。
 */
function lightenColor(color, factor) {
    let red = color.getRed() + (255 - color.getRed()) * factor;
    let green = color.getGreen() + (255 - color.getGreen()) * factor;
    let blue = color.getBlue() + (255 - color.getBlue()) * factor;

    red = Math.min(255, Math.max(0, red));
    green = Math.min(255, Math.max(0, green));
    blue = Math.min(255, Math.max(0, blue));

    return rgbToColor(red, green, blue);
}

/**
 * 计算给定颜色的互补色。
 * 互补色是通过将每个颜色分量从其原始值反转到 255 来计算的。
 * @param {Color} color - Java 的 Color 对象。
 * @return {Color} 计算后的互补色，返回 Java 的 Color 对象。
 */
function getComplementaryColor(color) {
    let red = 255 - color.getRed();
    let green = 255 - color.getGreen();
    let blue = 255 - color.getBlue();
    return rgbToColor(red, green, blue);
}

/**
 * 将颜色的 RGB 分量进行循环移位：R → G，G → B，B → R。
 * @param {Color} color - Java 的 Color 对象。
 * @return {Color} 移位后的颜色，返回 Java 的 Color 对象。
 */
function cycleRGB(color) {
    let red = color.getRed();
    let green = color.getGreen();
    let blue = color.getBlue();
    // 循环移位：R → G，G → B，B → R
    return rgbToColor(blue, red, green);
}

/**
 * 根据线路色映射方向箭头所用颜色。
 * @param {Color} color 线路色
 * @return {Color} 方向箭头所用颜色
 */
function arrowColor(color) {
    let r = color.getRed();
    let g = color.getGreen();
    let b = color.getBlue();

    // 获取颜色的HSL值
    let hsl = rgbToHsl(r, g, b);
    let hue = hsl[0]; // 色调（0-360）
    let saturation = hsl[1]; // 饱和度（0-100）
    let lightness = hsl[2]; // 亮度（0-100）

    // 直接映射到目标颜色
    let mappedColor;
    if (r > g && r > b) {
        // 红色系：R > G 和 R > B
        // 红色系 → 绿色 (50, 200, 0)
        mappedColor = rgbToColor(50, 200, 0);
    } else if ((r > 128 && g > 128 && b < 128) || (hue >= 30 && hue < 60)) {
        // 黄色系：R 和 G 都较高，B 较低
        // 橙色系：R > G > B
        // 黄色系和橙色系 → 绿色 (50, 200, 0)
        mappedColor = rgbToColor(50, 200, 0);
    } else if (b > r && b > g) {
        // 蓝色系：B > R 和 B > G
        // 蓝色系 → 绿色 (50, 200, 0)
        mappedColor = rgbToColor(50, 200, 0);
    } else if ((g > r && g > b) || (hue >= 60 && hue < 120)) {
        // 绿色系：G > R 和 G > B
        // 黄绿色系：G > B > R
        // 绿色系和黄绿色系 → 黄色 (230, 220, 0)
        mappedColor = rgbToColor(230, 220, 0);
    } else if ((g > 128 && b > 128 && r < 128) || (hue >= 120 && hue < 180)) {
        // 青色系：G 和 B 都较高，R 较低
        // 青绿色系：G > R > B
        // 青色系和青绿色系 → 黄色 (230, 220, 0)
        mappedColor = rgbToColor(230, 220, 0);
    } else if (isWhiteBlackGray(r, g, b)) {
        // 白色、黑色、灰色 → 浅蓝色 (100, 200, 200)
        mappedColor = rgbToColor(100, 200, 200);
    } else if (isPurpleBrownPink(r, g, b)) {
        // 紫色、棕色、粉色 → 黄色 (230, 220, 0)
        mappedColor = rgbToColor(230, 220, 0);
    } else {
        // 其他颜色映射到浅蓝色 (100, 200, 200)
        mappedColor = rgbToColor(100, 200, 200);
    }
    return mappedColor;
}

function isWhiteBlackGray(r, g, b) {
    // 白色：R, G, B 都较高且接近相等
    // 黑色：R, G, B 都较低且接近相等
    // 灰色：R, G, B 接近相等
    const threshold = 30; // 灰度阈值
    const maxDiff = Math.max(r, g, b) - Math.min(r, g, b);
    return maxDiff <= threshold;
}

function isPurpleBrownPink(r, g, b) {
    // 紫色：R 和 B 都较高，G 较低
    // 棕色：R 较高，G 和 B 较低
    // 粉色：R 较高，G 和 B 也较高，但整体亮度较高
    return (r > 128 && b > 128 && g < 128) || // 紫色
           (r > 128 && g < 128 && b < 128) || // 棕色
           (r > 128 && g > 128 && b > 128 && (r + g + b) > 500); // 粉色
}

function rgbToHsl(r, g, b) {//RGB色转HSL
    r /= 255;
    g /= 255;
    b /= 255;

    let max = Math.max(r, g, b);
    let min = Math.min(r, g, b);
    let delta = max - min;
    let hue = 0;
    if (delta === 0) {
        hue = 0;
    } else if (max === r) {
        hue = (60 * ((g - b) / delta) + 360) % 360;
    } else if (max === g) {
        hue = 60 * ((b - r) / delta + 2);
    } else if (max === b) {
        hue = 60 * ((r - g) / delta + 4);
    }

    let lightness = (max + min) / 2;
    let saturation = delta === 0 ? 0 : delta / (1 - Math.abs(2 * lightness - 1));

    return [hue, saturation * 100, lightness * 100];
}

//-----------------------------------功-能-类-函-数-----------------------------------
/**
 * 给 ModelCluster 设置统一颜色。（其实是调亮度用的）
 * @param {ModelCluster} modelCluster - 上传到显存后的模型对象，包含模型的渲染数据
 * @param {number} red - 目标颜色的红色通道值（0-255）
 * @param {number} green - 目标颜色的绿色通道值（0-255）
 * @param {number} blue - 目标颜色的蓝色通道值（0-255）
 * @param {number} alpha - 目标颜色的透明度通道值（0-255）
 */
function alterAllRGBA (modelCluster, red ,green , blue, alpha) {
    let vertarray = modelCluster.uploadedTranslucentParts.meshList;
    let vert = vertarray[0];
    for(let i = 0; i < vertarray.length; i++) {
        vert = vertarray[i];
        vert.materialProp.attrState.setColor(red , green , blue , alpha);
    }
    vertarray = modelCluster.uploadedOpaqueParts.meshList;
    vert = vertarray[0];
    for(let i = 0; i < vertarray.length; i++) {
        vert = vertarray[i];
        vert.materialProp.attrState.setColor(red , green , blue , alpha);
    }
}

//计算下一站信息的内容、字体大小、长度
function calculateNextStationTotalLength(g, nextStationInfo, height){
    let StationName = nextStationInfo.stationName;
    let interchangeInfo = nextStationInfo.interchangeInfo;
    let strToDrawCJK = "";
    let strToDrawNonCJK = "";
    let length_CJK = 0;
    let length_NonCJK = 0;
    let fontSize_CJK = 0;
    let fontSize_NonCJK = 0;

    let StationNameCJK = TextUtil.getCjkParts(StationName);
    let StationNameNonCJK = TextUtil.getNonCjkParts(StationName);

    //计算中文下一站信息和换乘信息长度
    if(!StationNameCJK.isEmpty()){
        strToDrawCJK = getMatching(LCD_ROUTE_NEXTSTATION, true) + "\u00A0" + StationNameCJK;
        if (interchangeInfo.length > 0) {
            strToDrawCJK += "\u00A0\u00A0\u00A0" + getMatching(LCD_ROUTE_INTERCHANGE, true);// 添加换乘标志
            // 使用 map 方法处理换乘信息数组
            strToDrawCJK += interchangeInfo.map((info, index) => {
                return (index > 0 ? "、" : "") + TextUtil.getCjkParts(info.name);
            }).join('');
        }
        fontSize_CJK = calculateMaxFontSizeN(g, SOURCE_HAN_SANS_CN_BOLD, strToDrawCJK, 0, height, 0, 0);
        let fm = g.getFontMetrics(SOURCE_HAN_SANS_CN_BOLD.deriveFont(fontSize_CJK));
        length_CJK = fm.stringWidth(strToDrawCJK);
    }

    //计算英文下一站信息和换乘信息长度
    if(!StationNameNonCJK.isEmpty()){
        strToDrawNonCJK = getMatching(LCD_ROUTE_NEXTSTATION, false) + "\u00A0" + StationNameNonCJK;
        if (interchangeInfo.length > 0) {
            strToDrawNonCJK += "\u00A0\u00A0\u00A0" + getMatching(LCD_ROUTE_INTERCHANGE, false) + "\u00A0";// 添加换乘标志
            // 使用 map 方法处理换乘信息数组
            strToDrawNonCJK += interchangeInfo.map((info, index) => {
                return (index > 0 ? ",\u00A0" : "") + TextUtil.getNonCjkParts(info.name);
            }).join('');
        }
        fontSize_NonCJK = calculateMaxFontSizeN(g, SANS_LAO, strToDrawNonCJK, 0, height, 0, 0);
        let fm = g.getFontMetrics(SANS_LAO.deriveFont(fontSize_NonCJK));
        length_NonCJK = fm.stringWidth(strToDrawNonCJK);
    }
    return{
        strToDrawCJK: strToDrawCJK,
        strToDrawNonCJK: strToDrawNonCJK,
        fontSize_CJK: fontSize_CJK,
        fontSize_NonCJK: fontSize_NonCJK,
        length_CJK: length_CJK,
        length_NonCJK: length_NonCJK
    }
}

//遍历并列出所有站点的换乘符号
function listStationInterchange(stationInfoList, station_Total){
    let interchangePart = {};
    for(let i = 0; i < station_Total; i++){
        let interchangeInfo = stationInfoList[i].interchangeInfo;
        interchangePart[i] = {};
        interchangePart[i].type = 0;//0为没有换乘，1为仅1线且不轮换，2为轮换
        interchangePart[i].interchangeDetails = [];
        interchangePart[i].const = 0;
        if(interchangeInfo && interchangeInfo.length > 0){
            let interchange_Total = interchangeInfo.length;
            let k = 0;
            for(let j = 0; j < interchange_Total; j++){
                let routeColor = interchangeInfo[j].color;
                let routeName = interchangeInfo[j].name;
                let routeName_CJK = TextUtil.getCjkParts(routeName);
                let routeName_Number1 = routeName_CJK.match(/^[^\u4e00-\u9fff]+/);//匹配中文线路名中的非CJK部分
                let routeName_Number2 = getMatching(routeName, false).match(/\d+/);
                let routeName_Number = null;
                if(routeName_Number1){//其次中文线路名中的非CJK部分（ 本想获取线路名||后{}内容，结果换乘线路名不包含||后的内容，乐）
                    routeName_Number = routeName_Number1[0];
                }else if(routeName_Number2){//再次英文线路名中的数字部分
                    routeName_Number = routeName_Number2[0];
                }else if(routeName_CJK){//最后中文线路名中"线"之前的部分
                    routeName_Number = routeName_CJK.match(/^(.*?)(线|$)/)[1];
                }//啥都不是，毁灭吧
                
                //根据类型储存换乘信息
                if(routeName_Number && routeName_Number.match(/[\u4e00-\u9fff]/g)){//有中文,CJK
                    let routeName_char = "" + routeName_Number;
                    for(let l = 0; l < routeName_char.length; l++){
                        interchangePart[i].interchangeDetails[k] = {}; // 初始化换乘信息对象
                        interchangePart[i].interchangeDetails[k].isCJK = true;
                        interchangePart[i].interchangeDetails[k].color = routeColor;
                        interchangePart[i].interchangeDetails[k].char = routeName_char[l];
                        k++;
                    }
                }else if(routeName_Number){//英文,非CJK
                    interchangePart[i].interchangeDetails[k] = {}; // 初始化换乘信息对象
                    interchangePart[i].interchangeDetails[k].isCJK = false;
                    interchangePart[i].interchangeDetails[k].color = routeColor;
                    interchangePart[i].interchangeDetails[k].char = routeName_Number;
                    k++;
                }
            }
            if(k == 1){//如果只有1个换乘信息
                interchangePart[i].type = 1;
            }else if(k > 1){
                interchangePart[i].type = 2;
            }
        }
    }
    return interchangePart;
}

//检查是否提前获取下一线路信息
function checkNextRoute(train, trainStatus, shouldCheck){
    if(shouldCheck && trainStatus == STATUS_ARRIVED && train.getThisRoutePlatformsNextIndex() == train.getThisRoutePlatforms().size() - 1 && getNextRoute(train, trainStatus) != null){//开启同台换向显示下一线路首发站信息显示时，只在到达终点站时执行检查，并且存在下一线路
        let thisRouteDestination = train.getThisRoutePlatforms().get(train.getThisRoutePlatforms().size() - 1);//本线路终点站

        if(thisRouteDestination.reverseAtPlatform){//如果不是同一站，则更新下一条线路信息
            return true;
        }
    }
    return false;
}

//获取本站在内的后三站 stationInfoList
function getStationInfoListNear3(train, trainStatus, thisStationIndex) {
    let stationInfoList = [];
    let routePlatforms = train.getAllPlatforms();

    // 检查 thisStationIndex 是否有效
    if (thisStationIndex < 0 || thisStationIndex >= routePlatforms.size()) {
        return stationInfoList; // 返回空数组
    }

    // 获取从 thisStationIndex 开始的最多三个站台的信息
    let maxIndex = Math.min(thisStationIndex + 3, routePlatforms.size());
    for (let i = thisStationIndex; i < maxIndex; i++) {
        let platformInfo = routePlatforms.get(i);
        if (platformInfo.station == null) {
            stationInfoList.push({
                platformName: platformInfo.platform.name,
                platformDwellTime: 0,
                stationName: UNKNOWN_STATION,
                interchangeInfo: []
            });
        } else {
            stationInfoList.push({
                platformName: platformInfo.platform.name,
                platformDwellTime: platformInfo.platform.dwellTime,
                reverseAtPlatform: platformInfo.reverseAtPlatform,
                stationName: platformInfo.station.name,
                interchangeInfo: getAllInterchangeRoutes(platformInfo.station, getNextRoute(train, trainStatus), null)
            });
        }
    }
    return stationInfoList;
}

function createMatrix(pos) {
    // 创建一个单位矩阵
    let matrix = new Matrices();

    // 应用位置变换
    matrix.translate(pos[0][0], pos[0][1], pos[0][2]);

    // 应用旋转变换
    // 注意：Matrix4f 的旋转方法需要单位向量和弧度制角度
    matrix.rotateX(pos[1]/180*Math.PI); // 绕 X 轴旋转
    matrix.rotateY(pos[2]/180*Math.PI); // 绕 Y 轴旋转
    matrix.rotateZ(pos[3]/180*Math.PI); // 绕 Z 轴旋转

    return matrix;
}

//检查两侧车门打开情况
function checkDoorOpnInfo(train){
    let isLeftDoorOpn = false;
    let isRightDoorOpn = false;
    for(let i = 0; i<train.trainCars(); i++){
        isLeftDoorOpn = isLeftDoorOpn || train.doorLeftOpen[i];
        isRightDoorOpn = isRightDoorOpn || train.doorRightOpen[i];
    }
    return{isLeftDoorOpn, isRightDoorOpn};
}

/*function detectDoorStatus(train, isRight) {//检查开关门信息，目前没用
    let paths = train.path();
    let pro = train.railProgress();

    let pf = train.getThisRoutePlatforms();
    let pfIndex = train.getThisRoutePlatformsNextIndex();
    let nextPF = null;
    if(pfIndex < 0 || pfIndex >= pf.size()){
        return null;
    }else{
        nextPF = pf[pfIndex];
    }
    let nextPF_distance = nextPF.distance;
    let index = train.getRailIndex(nextPF_distance, true);
    let pd = paths[index];
    let rail = pd.rail;
    let p0 = rail.getPosition(0);
    let p1 = rail.getPosition(rail.getLength());
    let p01 = new Vector3f(p0);
    let p02 = new Vector3f(p1);
    let bos = MinecraftClient.canOpenDoorsAt(p02, p01);//仅ANTE可用
    let o1 = bos[0];
    let o2 = bos[1];
    
    let open = -1; // 默认不开门
    if (isRight && o1) open = 0;
    if (!isRight && o2) open = 0;
    if (isRight && o2) open = 1;
    if (!isRight && o1) open = 1;
    if (o1 && o2) open = 2;
    
    return open;
}*/