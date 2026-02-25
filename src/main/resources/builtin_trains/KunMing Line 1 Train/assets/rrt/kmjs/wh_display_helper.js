
function DisplayHelperRE(cfg, stage, color) {
    if (cfg === void 0) return;

    this.cfg = cfg;
    if (cfg.version === 1) {
        let meshBuilder = new RawMeshBuilder(4, stage, Resources.idr("black.png"));
        meshBuilder.color(color[0], color[1], color[2], color[3]);
        for (let slotCfg of cfg.slots) {
            let realUV = Array(4);
            realUV[0] = [slotCfg.texArea[0] / cfg.texSize[0],
            slotCfg.texArea[1] / cfg.texSize[1]];
            realUV[1] = [slotCfg.texArea[0] / cfg.texSize[0],
            (slotCfg.texArea[1] + slotCfg.texArea[3]) / cfg.texSize[1]];
            realUV[2] = [(slotCfg.texArea[0] + slotCfg.texArea[2]) / cfg.texSize[0],
            (slotCfg.texArea[1] + slotCfg.texArea[3]) / cfg.texSize[1]];
            realUV[3] = [(slotCfg.texArea[0] + slotCfg.texArea[2]) / cfg.texSize[0],
            slotCfg.texArea[1] / cfg.texSize[1]];

            if (slotCfg.offsets === void 0) slotCfg.offset = [[0, 0, 0]];

            // 计算法向量
            let point0 = slotCfg.pos[0][0];
            let point1 = slotCfg.pos[0][1];
            let point2 = slotCfg.pos[0][2];
            let normal = calculateNormal(point0, point1, point2);

            for (let offset of slotCfg.offsets) {
                for (let posCfg of slotCfg.pos) {
                    for (let i = 0; i < 4; i++) {
                        meshBuilder
                            .vertex(posCfg[i][0] + offset[0], posCfg[i][1] + offset[1], posCfg[i][2] + offset[2])
                            .normal(normal[0], normal[1], normal[2])
                            .uv(realUV[i][0], realUV[i][1])
                            .endVertex();
                    }
                }
            }
        }

        let rawModel = new RawModel();
        rawModel.append(meshBuilder.getMesh());
        rawModel.triangulate();
        this.baseModel = ModelManager.uploadVertArrays(rawModel);
    } else {
        throw new Error("Unknown version: " + cfg.version);
    }
}

DisplayHelperRE.prototype.create = function () {
    let instance = new DisplayHelperRE();
    if (this.cfg.version === 1) {
        instance.texture = new GraphicsTexture(this.cfg.texSize[0], this.cfg.texSize[1]);
        instance._graphics = instance.texture.graphics;

        instance.emptyTransform = instance._graphics.getTransform();
        instance.slotTransforms = {};
        for (let slotCfg of this.cfg.slots) {
            instance._graphics.transform(java.awt.geom.AffineTransform.getTranslateInstance(slotCfg.texArea[0], slotCfg.texArea[1]));
            if (slotCfg.paintingSize !== void 0) {
                instance._graphics.transform(java.awt.geom.AffineTransform.getScaleInstance(slotCfg.texArea[2] / slotCfg.paintingSize[0],
                    slotCfg.texArea[4] / slotCfg.paintingSize[1]));
            }
            instance.slotTransforms[slotCfg.name] = instance._graphics.getTransform();
            instance._graphics.setTransform(instance.emptyTransform);
        }

        instance.model = this.baseModel.copyForMaterialChanges();
        instance.model.replaceAllTexture(instance.texture.identifier);
    } else {
        throw new Error("Unknown version: " + cfg.version);
    }
    return instance;
}

DisplayHelperRE.prototype.upload = function () {
    this.texture.upload();
}

DisplayHelperRE.prototype.close = function () {
    this.texture.close();
}

DisplayHelperRE.prototype.graphics = function () {
    this._graphics.setTransform(this.emptyTransform);
    return this._graphics;
}

DisplayHelperRE.prototype.graphicsFor = function (slotName) {
    this._graphics.setTransform(this.slotTransforms[slotName]);
    return this._graphics;
}

function calculateNormal(point0, point1, point2) {
    let vec0 = [point1[0] - point0[0], point1[1] - point0[1], point1[2] - point0[2]];
    let vec1 = [point2[0] - point0[0], point2[1] - point0[1], point2[2] - point0[2]];

    let normal = [
        vec0[1] * vec1[2] - vec0[2] * vec1[1],
        vec0[2] * vec1[0] - vec0[0] * vec1[2],
        vec0[0] * vec1[1] - vec0[1] * vec1[0]
    ];

    // 归一化法向量
    let length = Math.sqrt(normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2]);
    if (length !== 0) {
        normal = [
            normal[0] / length,
            normal[1] / length,
            normal[2] / length
        ];
    } else {
        // 如果长度为0，返回默认法向量（如 [0, 0, 0]）
        normal = [0, 0, 0];
    }

    return normal;
}