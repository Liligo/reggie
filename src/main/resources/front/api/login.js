function loginApi(data) {
    return $axios({
      'url': '/user/login',
      'method': 'post',
      data
    })
  }

function loginoutApi() {
  return $axios({
    'url': '/user/loginout',
    'method': 'post',
  })
}

function sendMsgApi(data) {
    return $axios({
        'url': 'http://localhost:8080/user/sendMsg',
        'method': 'post',
        data
    })
}

  