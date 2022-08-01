//avatar对象
function Avatar(qq = 2544193782) {
    const that = this
    this.avatarURL = `https://q.qlogo.cn/headimg_dl?dst_uin=${qq}&spec=640&img_type=jpg`
    fabric.Image.fromURL(this.avatarURL, a => {
        that.avatar = a
        that.avatar.scale(0.2)
        that.avatar.setControlsVisibility({
            mtr: false
        })
        that.avatar.uniformScaling = true
        canvas.add(that.avatar);
        let pos = [0, 0,
            Math.round(that.avatar.getScaledWidth()), Math.round(that.avatar.getScaledHeight())]
        $('#avatar_pos').text('坐标: [' + pos + ']')
        that.setPos(pos)
        that.avatar.on('moving', () => {
            avatarMoving()
        })
        that.avatar.on('scaling', () => {
            avatarMoving()
        })
    });

    //监听移动
    function avatarMoving() {
        let pos = [Math.round(that.avatar.left), Math.round(that.avatar.top),
            Math.round(that.avatar.getScaledWidth()), Math.round(that.avatar.getScaledHeight())]
        $('#avatar_pos').text('坐标: [' + pos + ']')
        that.setPos(pos)
    }

    this.isDelete = false
    this.delete = () => {
        canvas.remove(this.avatar)
        canvas.renderAll()
        document.getElementById('a' + that.id.toString()).remove()
        that.isDelete = true
    }

    this.type = 'TO';

    //坐标数组
    this.setPos = p => {
        frameLength === 1 ? that.pos = p : that.pos[backgroundId - 1] = p
    }

    this.id = avatarList.length
    $('#elementBar').append(`<div class="element avatar" id="a${this.id}"><div class="typeText">Avatar ${this.id}</div>` +
        '<div class="check" title="">round<input type="checkbox" class="round"></div>' +
        '<div class="check">avatarOnTop<input type="checkbox" class="avatarOnTop" checked></div>' +
        '<select><option>TO</option><option>FROM</option><option>GROUP</option><option>BOT</option></select>' +
        '<div class="check deleteAvatar">delete</div></div>')

    this.setRound = checked => {
        that.round = checked
        if (checked) {
            const roundedCorners = (avatar, radius) => new fabric.Rect({
                width: avatar.width,
                height: avatar.height,
                rx: radius / avatar.scaleX,
                ry: radius / avatar.scaleY,
                left: -avatar.width / 2,
                top: -avatar.height / 2
            })
            that.avatar.set("clipPath", roundedCorners(that.avatar,
                that.avatar.width < that.avatar.height ? (that.avatar.width / 2) : (that.avatar.height / 2)))
        } else {
            that.avatar.set("clipPath", null)
        }
        canvas.renderAll()
    }

    this.onTop = true
    this.round = false
    this.pos = new Array(frameLength).fill([0, 0, 0, 0])

    this.build = () => {
        let builtPos = []
        switch (imageType) {
            case 'IMG':
                builtPos = that.pos
                break
            case 'GIF':
                for (const posEle of that.pos) {
                    builtPos.push(`[${posEle}]`)
                }
                break
        }
        return `{
        "type": "${that.type}",
        "pos": [${builtPos}],
        "round": ${that.round},
        "avatarOnTop": ${that.onTop}
    }`
    }
}

//round
$('#elementBar').on('change', '.avatar .round', function () {
    console.log(this.parentNode.parentNode.id)
    avatarList[this.parentNode.parentNode.id.slice(1)].setRound(this.checked)
})

    //avatarOnTop
    .on('change', '.avatar .avatarOnTop', function () {
        const avatarEle = avatarList[this.parentNode.parentNode.id.slice(1)]
        console.log(avatarEle)
        avatarEle.onTop = this.checked
        if (avatarList.length !== 1) {
            avatarEle.avatar.opacity = this.checked ? 1 : 0.6
            canvas.renderAll()
            return
        }
        if (this.checked) {
            canvas.backgroundImage = backGroundImage
            canvas.overlayImage = null
        } else {
            canvas.overlayImage = backGroundImage
            canvas.backgroundImage = null
        }
        canvas.renderAll()
    })

    //changeType
    .on('change', '.avatar select', function () {
        avatarList[this.parentNode.id.slice(1)].type = this.value
    })

    //deleteAvatar
    .on('click', '.avatar .deleteAvatar', function () {
        avatarList[this.parentNode.id.slice(1)].delete()
    })

let avatarList = [];

function addAvatar() {
    avatarList.push(new Avatar())
}