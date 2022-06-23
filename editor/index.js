// 文件上传
$(function () { //拖拽
    $(document).on({
        dragleave: function (e) {
            e.preventDefault();
        },
        drop: function (e) {
            e.preventDefault();
        },
        dragenter: function (e) {
            e.preventDefault();
        },
        dragover: function (e) {
            e.preventDefault();
        }
    });
    const box = document.getElementById('drop_area');
    box.addEventListener("drop", function (e) {
        e.preventDefault();
        const fileList = e.dataTransfer.files;
        if (fileList.length === 0) {
            return false;
        }
        loadImage(fileList)
    }, false);
});

function chooseFile() { //选择
    $('#file').click().change(function () {
        const fileList = $('#file').prop('files')
        loadImage(fileList)
    })
}

let imageType = 'IMG'

// 加载图片
function loadImage(fileList) {
    if (fileList[0].type.indexOf('image') === -1) {
        alert("仅支持图片格式");
        return false;
    }
    $('#drop_area').hide()
    $('.bar').slideDown()
    const reader = new FileReader();
    const image = new Image()
    reader.readAsDataURL(fileList[0]);
    reader.onload = function () {
        image.src = reader.result.toString()
    }
    if (fileList[0].type === 'image/gif') {
        imageType = 'GIF'
        loadGif(image);
        return;
    }
    image.onload = function () {
        loadBackGround(image)
    }
}

const canvas = new fabric.Canvas('app');
let backGroundImage;
let backgroundId = 0
let frameLength = 1

//加载GIF
function loadGif(image) {
    image.id = 'gifLoader'
    $('#gifBar').append(image)
    document.getElementById('gifLoader').onload = function () {
        let gif = new RubbableGif({gif: document.getElementById('gifLoader')})
        gif.load(function () {
            $('.jsgif').hide()
            for (let i = 1; i <= gif.get_length(); i++) {
                gif.move_to(i);
                let frameImage = new Image();
                frameImage.src = gif.get_canvas().toDataURL('image/png', 1);
                frameImage.id = 'f' + i
                frameImage.className = 'frame'
                $('#gifBar').append(frameImage)
            }
            frameLength = gif.get_length()
        })
        $('#set').append('<div class="check" onclick="downloadAllBackground()">下载每一帧</div>')
    }
}

//设置画布背景
function loadBackGround(img) {
    const image = new fabric.Image(img)
    backGroundImage = image

    $('#bg_size').text('画布: ' + image.width + '*' + image.height)
    canvas.setWidth(image.width)
    canvas.setHeight(image.height)
    canvas.backgroundImage = image
    canvas.renderAll()
}

// 切换GIF帧
$('#gifBar').on('click', '.frame', function () {
    const backgroundImage = new Image()
    backgroundImage.src = this.src
    backgroundId = parseInt(this.id.replace('f', ''))
    loadBackGround(backgroundImage)
})

//下载
function downloadAllBackground() {
    const imgNum = document.getElementsByClassName('frame').length
    if (!imgNum) return
    const zip = new JSZip()
    const imgs = zip.folder('petpet-gif');
    for (let i = 0; i < imgNum; i++) {
        let raw = document.getElementsByClassName('frame').item(i).src.split(';base64,')[1];
        console.log(raw)
        imgs.file(i + '.png', raw, {base64: true});
    }
    zip.generateAsync({type: "blob"})
        .then(function (content) {
            saveAs(content, "petpet.zip");
        });
}