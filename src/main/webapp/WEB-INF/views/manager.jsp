<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<meta content="text/html; charset=UTF-8" http-equiv="content-type">
		<title>Mediaserver - Konfiguration</title>
	</head>
	<body bgcolor="lightgrey" >		
		<table border="0" width="100%" height="100%">
			<thead>
				<tr>
					<td>		
						<h1 align="center">Willkommen auf dem Mediaserver</h1>
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
														<h2 align="center">Einstellungen</h2>
													</td>
												</tr>
												<tr>
													<td width="100%">
														<form accept-charset="utf8" enctype="application/x-www-form-urlencoded" method="POST" action="settings/update">
															<table border="0" width="100%" border="0" bgcolor="lightblue" cellpadding="5" cellspacing="2" align="left">
																<tbody>
																	<tr>
																		<td nowrap="nowrap">Sichtbarer Name</td>
																		<td >
																			<c:out value="${server.name}" />
																		</td>
																		<td >
																			<input value="<c:out value="${server.name}"/>" name="name" type="text" size="100%" />
																		</td>
																	</tr>
																	<tr>
																		<td nowrap="nowrap">Webseite</td>
																		<td >
																			<c:out value="${server.url}" />
																		</td>
																		<td >
<%-- 																			<input value="<c:out value="${server.port}"/>" name="port" type="text" size="100%" /> --%>
																		</td>
																	</tr>
																	<tr>
																		<td nowrap="nowrap">Netzwerkkarte</td>
																		<td  nowrap="nowrap">
																			<c:out value="${server.networkInterface}" />
																		</td>
																		<td  nowrap="nowrap">
																			<input value="<c:out value="${server.networkInterface}"/>" name="networkInterface" type="text" size="100%" />
																			<font color="orange">(*)</font>
																		</td>
																	</tr>
																	<tr>
																		<td nowrap="nowrap">Ordner f&uuml;r Previews</td>
																		<td  nowrap="nowrap">
																			<c:out value="${server.previews}" />
																		</td>
																		<td >
																			<input value="<c:out value="${server.previews}"/>" name="previews" type="text" size="100%" />
																		</td>
																	</tr>
																	<tr>
																		<td nowrap="nowrap">Pfad zu MPlayer</td>
																		<td  nowrap="nowrap">
																			<c:out value="${server.mplayer}" />
																		</td>
																		<td  nowrap="nowrap">
																			<input value="<c:out value="${server.mplayer}"/>" name="mplayer" type="text" size="100%" />
																		</td>
																	</tr>
																	<tr>
																		<td nowrap="nowrap">Pfad zu MEncoder</td>
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
																		<td align="right">
																			<font size="1">(*) Neustart erforderlich</font>										
																		</td>
																	</tr>
																	<tr>
																		<td></td>
																		<td></td>
																		<td align="left">
																			<button type="submit" value="settings" name="update">&Uuml;bernehmen</button>
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
														<h2 align="center">Freigaben</h2>
													</td>
												</tr>
												<tr>
													<td width="100%">
														<h3>Ordner freigeben</h3>
													</td>
												</tr>
												<tr>
													<td width="100%">
														<form accept-charset="utf8" enctype="application/x-www-form-urlencoded" method="POST" action="scanfolder/add">
															<table border="0" width="100%" border="0" cellpadding="5" cellspacing="2">
																<tbody>
																	<tr>
																		<td nowrap="nowrap" align="center">
																			<button type="submit" name="folder_bt" value="create">Hinzuf&uuml;gen</button>
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
														<h3>&Uuml;bersicht der vorhandenen Freigaben</h3>
													</td>
												</tr>
												<tr>
													<td width="100%">
														<table border="0" width="100%" cellpadding="5" cellspacing="2">
															<tbody>
																<tr>
																	<th></th>
																	<th nowrap="nowrap" align="center">Status</th>
																	<th nowrap="nowrap" align="center">Ordner</th>
																	<th nowrap="nowrap" align="center">enhaltene Ordner</th>
																	<th nowrap="nowrap" align="center">enthaltene Dateien</th>
																	<th nowrap="nowrap" align="center">Gesamtgr&ouml;&szlig;e</th>
																	<th nowrap="nowrap" align="center">Letzte Pr&uuml;fung</th>
																	<th nowrap="nowrap" align="center">Pr&uuml;fintervall in Minuten</th>																
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
																	<td  nowrap="nowrap" align="right", valign="middle">
																		<form accept-charset="utf8"	enctype="application/x-www-form-urlencoded" method="POST" action="scanfolder/remove">
																			<button type="submit" value="${folder.id}" name="folder">Entfernen</button>
																		</form>
																	</td>
																	<td nowrap="nowrap" align="center">${folder.scanState}</td>
																	<td nowrap="nowrap" align="justify">${folder.path}</td>
																	<td nowrap="nowrap" align="center">${folder.folderCount}</td>
																	<td nowrap="nowrap" align="center">${folder.fileCount}</td>
																	<td nowrap="nowrap" align="center">${folder.size}</td>
																	<td nowrap="nowrap" align="center">${folder.lastScan}</td>
																	<td nowrap="nowrap" align="center">
																		<form accept-charset="utf8"	enctype="application/x-www-form-urlencoded" method="POST" action="scanfolder/update">
																			<input type="text" name="scan_interval" value="${folder.scanInterval}" size="8"/> 
																			<button type="submit" value="${folder.id}" name="folder">&Auml;ndern</button>		
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