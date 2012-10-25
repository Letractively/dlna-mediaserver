<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ page session="false"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<meta content="text/html; charset=UTF-8" http-equiv="content-type">
		<title><spring:message code="website.settings.title"/></title>
	</head>
	<body bgcolor="lightgrey" >		
		<table border="0" width="100%" height="100%">
			<thead>
				<tr>
					<td>		
						<h1 align="center"><spring:message code="website.welcome"/></h1>
					</td>
				</tr>				
			</thead>
			<tbody>	
				<tr align="center" valign="top">
<!-- 					<td bgcolor="darkgrey"> -->
<!-- 						<table  width="50" border="0"  align="left" height="50" cellpadding="5" cellspacing="2" > -->
<!-- 							<tbody valign="top"> -->
<!-- 								<tr valign="top"> -->
<!-- 									<td width="100%">	 -->
<!-- 										<a target="manager" href="/manager">Einstellungen</a> -->
<!-- 									</td> -->
<!-- 								</tr> -->
<!-- 								<tr> -->
<!-- 									<td width="100%"> -->
<!-- 										<a target="content" href="/content">Inhalt</a> -->
<!-- 									</td> -->
<!-- 								</tr> -->
<!-- 							</tbody> -->
<!-- 						</table> -->
<!-- 					</td> -->
					<td>
						<table border="0" width="70%" align="center">
							<tbody>
								<tr>
									<td>
										<table border="0" width="100%">
											<tbody>
												<tr>
													<td width="100%">
														<h2 align="center"><spring:message code="website.settings"/></h2>
													</td>
												</tr>
												<tr>
													<td width="100%">
														<form accept-charset="utf8" enctype="application/x-www-form-urlencoded" method="POST" action="settings/update">
															<table border="0" width="100%" border="0" bgcolor="lightblue" cellpadding="5" cellspacing="2" align="left">
																<tbody>
																	<tr>
																		<td nowrap="nowrap"><spring:message code="website.settings.visible_name"/></td>
																		<td >
																			<c:out value="${server.name}" />
																		</td>
																		<td >
																			<input value="<c:out value="${server.name}"/>" name="name" type="text" size="100%" />
																		</td>
																	</tr>
																	<tr>
																		<td nowrap="nowrap"><spring:message code="website.settings.url"/></td>
																		<td >
																			<c:out value="${server.url}" />
																		</td>
																		<td >
<%-- 																			<input value="<c:out value="${server.port}"/>" name="port" type="text" size="100%" /> --%>
																		</td>
																	</tr>
																	<tr>
																		<td nowrap="nowrap"><spring:message code="website.settings.network_device"/></td>
																		<td  nowrap="nowrap">
																			<c:out value="${server.networkInterface}" />
																		</td>
																		<td  nowrap="nowrap">
																			<input value="<c:out value="${server.networkInterface}"/>" name="networkInterface" type="text" size="100%" />
																		</td>
																	</tr>
																	<tr>
																		<td nowrap="nowrap"><spring:message code="website.settings.preview_folder"/></td>
																		<td  nowrap="nowrap">
																			<c:out value="${server.previews}" />
																		</td>
																		<td >
																			<input value="<c:out value="${server.previews}"/>" name="previews" type="text" size="100%" />
																		</td>
																	</tr>
																	<tr>
																		<td nowrap="nowrap"><spring:message code="website.settings.mplayer_path"/></td>
																		<td  nowrap="nowrap">
																			<c:out value="${server.mplayer}" />
																		</td>
																		<td  nowrap="nowrap">
																			<input value="<c:out value="${server.mplayer}"/>" name="mplayer" type="text" size="100%" />
																		</td>
																	</tr>
																	<tr>
																		<td nowrap="nowrap"><spring:message code="website.settings.mencoder_path"/></td>
																		<td  nowrap="nowrap">
																			<c:out value="${server.mencoder}" />
																		</td>
																		<td  nowrap="nowrap">
																			<input value="<c:out value="${server.mencoder}"/>" name="mencoder" type="text" size="100%"/>
																		</td>
																	</tr>
																	<tr>
																		<td></td>
																		<td></td>
																		<td align="left">
																			<button type="submit" value="settings" name="update"><spring:message code="website.settings.submit"/></button>
																		</td>
																	</tr>
																	
																</tbody>
															</table>
														</form>
													</td>
												</tr>
											</tbody>
										</table>
									</td>
								</tr>
								<tr>
									<td>
										<table border="0" width="100%">
											<tbody>
												<tr>
													<td width="100%">
														<h2 align="center"><spring:message code="website.shares.title"/></h2>
													</td>
												</tr>
												<tr>
													<td width="100%">
														<h3><spring:message code="website.shares.add.title"/></h3>
													</td>
												</tr>
												<tr>
													<td width="100%">
														<form accept-charset="utf8" enctype="application/x-www-form-urlencoded" method="POST" action="scanfolder/add">
															<table border="0" width="100%" border="0" cellpadding="5" cellspacing="2">
																<tbody>
																	<tr>
																		<td nowrap="nowrap" align="center">
																			<button type="submit" name="folder_bt" value="create"><spring:message code="website.shares.add.submit"/></button>
																			<input name="folder" type="text" size="100%"/>
																		</td>
																	</tr>
																</tbody>
															</table>
														</form>
													</td>
												</tr>
												<tr>
													<td width="100%">
														<h3><spring:message code="website.shares.current.title"/></h3>
													</td>
												</tr>
												<tr>
													<td width="100%">
														<table border="0" width="100%" cellpadding="5" cellspacing="2">
															<tbody>
																<tr>
																	<th></th>
																	<th nowrap="nowrap" align="center"><spring:message code="website.shares.current.state"/></th>
																	<th nowrap="nowrap" align="center"><spring:message code="website.shares.current.folder"/></th>
																	<th nowrap="nowrap" align="center"><spring:message code="website.shares.current.folder_count"/></th>
																	<th nowrap="nowrap" align="center"><spring:message code="website.shares.current.file_count"/></th>
																	<th nowrap="nowrap" align="center"><spring:message code="website.shares.current.size"/></th>
																	<th nowrap="nowrap" align="center"><spring:message code="website.shares.current.last_scan"/></th>
																	<th nowrap="nowrap" align="center"><spring:message code="website.shares.current.scan_interval"/></th>																
																</tr>
																<c:forEach var="folder" items="${folderList}" varStatus="rowCounter">
															      <c:choose>
															          <c:when test="${rowCounter.count % 2 == 0}">
															            <c:set var="rowStyle" scope="page" value="lightgreen"/>
															          </c:when>
															          <c:otherwise>
															            <c:set var="rowStyle" scope="page" value="lightyellow"/>
															          </c:otherwise>
															        </c:choose>																
																	<tr bgcolor="${rowStyle}">
																		<td  nowrap="nowrap" align="right" valign="middle">
																			<form accept-charset="utf8"	enctype="application/x-www-form-urlencoded" method="POST" action="scanfolder/remove">
																				<button type="submit" value="${folder.id}" name="folder"><spring:message code="website.shares.current.remove"/></button>
																			</form>
																		</td>
																		<td nowrap="nowrap" align="center"><spring:message code="${folder.scanState}"/></td>
																		<td nowrap="nowrap" align="justify">${folder.path}</td>
																		<td nowrap="nowrap" align="center">${folder.folderCount}</td>
																		<td nowrap="nowrap" align="center">${folder.fileCount}</td>
																		<td nowrap="nowrap" align="center">${folder.size}</td>
																		<td nowrap="nowrap" align="center"><spring:message text="${folder.lastScan}" /></td>
																		<td nowrap="nowrap" align="center">
																			<form accept-charset="utf8"	enctype="application/x-www-form-urlencoded" method="POST" action="scanfolder/update">
																				<input type="text" name="scan_interval" value="${folder.scanInterval}" size="8"/> 
																				<button type="submit" value="${folder.id}" name="folder"><spring:message code="website.shares.current.adjust"/></button>		
																			</form>																	
																		</td>
																	</tr>
																</c:forEach>
															</tbody>
														</table>
													</td>
												</tr>
											</tbody>
										</table>									
									</td>
								</tr>								
							</tbody>	
							<tfoot>
								<tr>
									<td>
									</td>
								</tr>
							</tfoot>												
						</table>
					</td>
				</tr>
			</tbody>
		</table>
	</body>
</html>