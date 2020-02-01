function toggleUploadDiv() {
  var x = document.getElementById("uploadDiv");
  if (x.style.display === "none") {
    x.style.display = "block";
  } else {
    x.style.display = "none";
  }
}

function addAlbum() {
  var info = prompt('请输入相册名:', '快乐的青海湖之旅');
  if (info != null && info != '') {
    var form = document.getElementById('form_add_album');
    var input = document.getElementById('album');
    input.value = info;
    form.submit();
  }
}
