/* 直接在bot帮助首页index.html中引入 */
/*
var xhr = new XMLHttpRequest();
xhr.open('get','api/v1/jrbot/bot_update');
xhr.send();
xhr.onload=function(){
	if(xhr.readyState==4){
		const response = JSON.parse(xhr.responseText);
		console.log(response);
		if(response.success){
			var data = response.contents;
			var node = document.getElementById('update-datas');
			node.removeChild(document.getElementById('pending'));
			for (var c of data){
				var child = document.createElement('tr');
				var d = document.createElement('td');
				d.textContent = c.version;
				var d2 = document.createElement('td');
				d2.textContent = c.update_date;
				var d3 = document.createElement('td');
				d3.textContent = c.content;
				child.append(d);
				child.append(d2);
				child.append(d3);
				node.appendChild(child);
			}
		}
	}
}
*/
