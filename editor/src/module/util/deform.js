export const polygonPositionHandler = function (dim, finalMatrix, fabricObject) {
    let x = (fabricObject.points[this.pointIndex].x - fabricObject.pathOffset.x),
        y = (fabricObject.points[this.pointIndex].y - fabricObject.pathOffset.y)
    return fabric.util.transformPoint(
        new fabric.Point(x, y),
        fabric.util.multiplyTransformMatrices(
            fabricObject.canvas.viewportTransform,
            fabricObject.calcTransformMatrix(), false
        )
    )
}

export const getObjectSizeWithStroke = object => {
    const stroke = new fabric.Point(
        object.strokeUniform ? 1 / object.scaleX : 1,
        object.strokeUniform ? 1 / object.scaleY : 1
    ).multiply(object.strokeWidth)
    return new fabric.Point(object.width + stroke.x, object.height + stroke.y)
}

export const actionHandler = (eventData, transform, x, y) => {
    const polygon = transform.target,
        currentControl = polygon.controls[polygon.__corner],
        mouseLocalPosition = polygon.toLocalPoint(new fabric.Point(x, y), 'center', 'center'),
        polygonBaseSize = getObjectSizeWithStroke(polygon),
        size = polygon._getTransformedDimensions(0, 0);
    polygon.points[currentControl.pointIndex] = {
        x: mouseLocalPosition.x * polygonBaseSize.x / size.x + polygon.pathOffset.x,
        y: mouseLocalPosition.y * polygonBaseSize.y / size.y + polygon.pathOffset.y
    }
    return true
}

export const anchorWrapper = (anchorIndex, fn) => {
    return function (eventData, transform, x, y) {
        const fabricObject = transform.target,
            absolutePoint = fabric.util.transformPoint({
                x: (fabricObject.points[anchorIndex].x - fabricObject.pathOffset.x),
                y: (fabricObject.points[anchorIndex].y - fabricObject.pathOffset.y),
            }, fabricObject.calcTransformMatrix()),
            actionPerformed = fn(eventData, transform, x, y),
            newDim = fabricObject._setPositionDimensions({}),
            polygonBaseSize = getObjectSizeWithStroke(fabricObject),
            newX = (fabricObject.points[anchorIndex].x - fabricObject.pathOffset.x) / polygonBaseSize.x,
            newY = (fabricObject.points[anchorIndex].y - fabricObject.pathOffset.y) / polygonBaseSize.y
        fabricObject.setPositionByOrigin(absolutePoint, newX + 0.5, newY + 0.5)
        return actionPerformed
    }
}