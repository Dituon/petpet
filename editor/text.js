function Text(text) {
    const that = this
    this.text = new fabric.IText(text, {fill: '#191919', fontSize: 128});
    this.text.setControlsVisibility({
        mb: false,
        ml: false,
        mr: false,
        mt: false,
        mtr: false
    })
    canvas.add(this.text);

    this.text.on('moving', function () {
        textMoving()
    })
    this.text.on('scaling', function (e) {
        if (e.transform.target.scaleX === e.transform.target.scaleY) {
            let size = e.transform.target.scaleX * 200
            that.text.set('fontSize', size)
            document.getElementById('t' + that.id).querySelector('.typein').value =
                Math.round(size / 16)
        }
        textMoving()
    })

    this.pos = [0, 0]

    function textMoving() {
        that.pos = [Math.round(that.text.left), Math.round(that.text.top)]
        $('#avatar_pos').text('坐标: [' + that.pos + ']')
    }

    this.isDelete = false
    this.delete = function () {
        canvas.remove(this.text)
        canvas.renderAll()
        document.getElementById('t' + that.id.toString()).remove()
        that.isDelete = true
    }

    this.build = function () {
        return `{
        "text": "${that.text.get('text')}",
        "pos": [${that.pos}],
        "color": "${that.text.get('fill')}",
        "size": ${Math.round(that.text.get('fontSize') / 16)}
    }`
    }

    this.id = textList.length
    $('#elementBar').append(`<div class="element text" id="t${this.id}"><div class="typeText">Text ${this.id}</div>` +
        '<div class="check" title="">color<input type="color" class="color"></div>' +
        '<div class="check">size<input type="number" class="typein" value="8"></div>' +
        '<div class="check deleteText">delete</div></div>')
}

$('#elementBar').on('change', '.text .color', function () {
    textList[this.parentNode.parentNode.id.slice(1)].text.set('fill', this.value)
    canvas.renderAll()
})

$('#elementBar').on('change', '.text .typein', function () {
    textList[this.parentNode.parentNode.id.slice(1)].text.set('fontSize', this.value * 16)
    canvas.renderAll()
})

$('#elementBar').on('click', '.text .deleteText', function () {
    textList[this.parentNode.id.slice(1)].delete()
})

let textList = [];

function addText() {
    textList.push(new Text('petpet!'))
}