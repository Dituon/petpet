function Text(text) {
    const that = this
    this.text = new fabric.IText(text, {fill: '#191919', fontSize: 56});
    this.text.setControlsVisibility({
        mb: false,
        ml: false,
        mr: false,
        mt: false,
        mtr: false
    })
    canvas.add(this.text);

    this.text.on('moving', () => {
        textMoving()
    })
    this.text.on('scaling', e => {
        if (e.transform.target.scaleX === e.transform.target.scaleY) {
            let size = that.text.fontSize * e.transform.target.scaleX
            document.getElementById('t' + that.id).querySelector('.typein').value =
                Math.round(size * 0.75)
        }
        textMoving()
    })
    this.text.on('changing', () => {
        textMoving()
    })

    this.pos = [0, 0]

    function textMoving() {
        let x = Math.round(that.text.left)
        let y = Math.round(that.text.top)
        switch (that.align) {
            case 'RIGHT':
                x += Math.round(that.text.getScaledWidth())
                break
            case 'CENTER':
                x += Math.round(that.text.getScaledWidth() / 2)
                y += Math.round(that.text.getScaledHeight() / 2)
                break
        }

        that.pos[0] = x
        that.pos[1] = y
        $('#avatar_pos').text('坐标: [' + that.pos + ']')
    }

    this.align = 'LEFT'
    this.changeAlign = value => {
        that.align = value
        switch (value) {
            case 'LEFT':
                that.text.textAlign = 'left'
                break
            case 'RIGHT':
                that.text.textAlign = 'right'
                break
            case 'CENTER':
                that.text.textAlign = 'center'
                break
        }
        textMoving()
    }

    this.wrap = 'NONE'
    this.changeWrap = value => {
        console.log(value)
        that.wrap = value
        switch (value) {
            case 'NONE':
                $(`#t${that.id} .check.setWidth`).slideUp()
                that.setMaxWidth(0)
                break
            default:
                console.log(0)
                $(`#t${that.id} .check.setWidth`).slideDown()
                that.setMaxWidth(Math.round(that.text.getScaledWidth()))
                break
        }
    }

    this.maxWidth = 0
    this.setMaxWidth = width => {
        that.maxWidth = width
        if (width === 0) {
            that.pos.length = 2
        } else {
            $(`#t${that.id} .check.setWidth .typein.width`).val(width)
            that.pos[2] = width
        }
        textMoving()
    }

    this.isDelete = false
    this.delete = () => {
        canvas.remove(this.text)
        canvas.renderAll()
        document.getElementById('t' + that.id.toString()).remove()
        that.isDelete = true
    }

    this.build = () => {
        let extra = (that.align === 'LEFT' ? '' : `,\n        "align": "${that.align}"`) +
            (that.wrap === 'NONE' ? '' : `,\n        "wrap": "${that.wrap}"`)
        return `{
        "text": "${that.text.get('text')}",
        "pos": [${that.pos}],
        "color": "${that.text.get('fill')}",
        "size": ${Math.round(that.text.get('fontSize') * that.text.get('scaleX') * 0.75)}${extra}
    }`
    }

    this.id = textList.length
    $('#elementBar').append(`
    <div class="element text" id="t${this.id}">
        <div class="typeText">Text ${this.id}</div>
        <div class="check" title="">color<input type="color" class="color"></div>
        <div class="check">size<input type="number" class="typein size" value="42"></div>
        <select class="textAlign">
            <option value="LEFT">左对齐</option>
            <option value="RIGHT">右对齐</option>
            <option value="CENTER">居中</option>
        </select>
        <select class="textWrap">
            <option value="NONE">不换行</option>
            <option value="BREAK">自动换行</option>
            <option value="ZOOM">自动缩放</option>
        </select>
        <div class="check setWidth" style="display: none">maxWidth<input type="number" class="typein width" value="0"></div>
        <div class="check deleteText">delete</div>
    </div>`)
}

$('#elementBar').on('change', '.text .color', function () {
    textList[this.parentNode.parentNode.id.slice(1)].text.set('fill', this.value)
    canvas.renderAll()
})

    .on('change', '.text .check .typein.size', function () {
        textList[this.parentNode.parentNode.id.slice(1)].text.set('fontSize', this.value * (4/3))
        canvas.renderAll()
    })

    .on('click', '.text .deleteText', function () {
        textList[this.parentNode.id.slice(1)].delete()
    })

    .on('change', '.text select.textAlign', function () {
        textList[this.parentNode.id.slice(1)].changeAlign(this.value)
    })

    .on('change', '.text select.textWrap', function () {
        textList[this.parentNode.id.slice(1)].changeWrap(this.value)
    })

    .on('change', '.text .check .typein.width', function () {
        textList[this.parentNode.parentNode.id.slice(1)].setMaxWidth(this.value)
    })

let textList = [];

function addText() {
    textList.push(new Text('petpet!'))
}