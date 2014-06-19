function PPY_postLoginForm(token){
	if(token != null ){
		$("#loginform").find("input[name='token']").val(token);
		 $("#loginform").find("input[name='grant_type']").val("facebook");
	}	
	 $.ajax({
      url: '/oauth2/v1/auth/token',
      type: 'post',
      dataType: "json",
      data : $('#loginform').serialize(),
      success: function(data, response, xhr){
        if(response.error_description){
        	$("#error_alert").text(data.error_description);
        	$("#error_alert").show();
        }else{
        	if(data.code == 302 && data.code == 307){
        		window.location.replace(data.message);
        	}else{
				fragment = "access_token=" + data.access_token +
				"&state="+data.state +
				"&token_type="+data.token_type +
				"&expires_in="+data.expires_in +
				"&scope="+ encodeURIComponent(data.scope);
				window.location.replace(oa2_values.redirect_uri + '#' + fragment);  
        	}
        }
      },
      error: function(data, response){
      	$("#error_alert").text("Ha sucedido un error, por favor vuelva a intentar " +
      			"o consulte con el soporte técnico.");
    	$("#error_alert").show();
      }
    });
};

function getUrlVars(){
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++){
    	hash = hashes[i];
    	key = hash.substring(0,hash.indexOf("="));
    	val = hash.substring(hash.indexOf("=")+1, hash.length);
    	vars.push(key);
        vars[key] = val;
    }
    return vars;
};

function PPY_initLoginForm(){
    $("#error_alert").hide();
    oa2_values = getUrlVars();
    $("#loginform").find("input[name='client_id']").val(oa2_values.client_id);
    $("#loginform").find("input[name='grant_type']").val(oa2_values.grant_type);
    $("#loginform").find("input[name='response_type']").val(oa2_values.response_type);
    $("#loginform").find("input[name='redirect_uri']").val(oa2_values.redirect_uri); 
    $("a[href='#reset_pass']").prop("href", oa2_values.redirect_uri + "#reset_password=true");
    $("a[href='#requires_new_user']").prop("href", oa2_values.redirect_uri + "#requires_new_user=true");
};

//deprecated
function PPY_facebookLogin() {
	console.log("llamando login de facebook.");
	
	FB.login(function(response) {
		           if (response.authResponse) {
		        	   console.log("llamando login de facebook.");
		             var access_token = response.authResponse.accessToken;
		             PPY_postLoginForm(access_token);
		           }else {
		             console.log('User cancelled login or did not fully authorize.');
		}
	});
};


function switchImage(img, event) {
		if(event === "down") img.src = "img/active_200.png";
   	else if(event === "up") img.src = "img/pressed_200.png";
};
