<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script src="http://ajax.microsoft.com/ajax/jquery/jquery-1.10.2.js" type="text/javascript"></script> 
<title>Insert title here</title>
<link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" />

</head>
<body>
<h1>Upload a file</h1>
<form id="sendFileForm" action="http://localhost:8080/CC_Group_Project_PaaS/home/upload" method="post" enctype="multipart/form-data">
	<label>Select File</label><input type= "file" name="file" /> <br/><br/>
	<input type="submit" title="Save"/>
</form>
hello jsp. I am at: http://localhost:8080/CC_Group_Project_PaaS/home/index (see web.xml)



</body>
</html>