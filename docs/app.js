/* Gran Zona Marciana: vista de portfolio. Datos ficticios, sin backend ni API Android. */
(() => {
"use strict";
const KEY="gzm-portfolio-v2";
const DAY=86400000;
const offset=(days,hours=0)=>new Date(Date.now()+days*DAY+hours*3600000).toISOString();
const photos=[
 "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=520&h=550&fit=crop",
 "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=520&h=550&fit=crop",
 "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=520&h=550&fit=crop",
 "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=520&h=550&fit=crop"
];
function seed(){
 return {
   edition:{id:1,nombre:"Temporada 01",max:5,inicio:offset(-2),fin:offset(24)},
   galas:[
     {id:1,nombre:"Gala de apertura",fecha:offset(0,-3),resumen:"Conoce a los participantes que llegan al universo marciano."},
     {id:2,nombre:"La primera misión",fecha:offset(4,0),resumen:"Una prueba inesperada cambiará las alianzas."},
     {id:3,nombre:"Noche de decisiones",fecha:offset(11,0),resumen:"El público tendrá la última palabra."}
   ],
   contestants:[
     {id:11,nombre:"Álex Moreno",alias:"El estratega",foto:photos[0]},
     {id:12,nombre:"Claudia Vega",alias:"La exploradora",foto:photos[1]},
     {id:13,nombre:"David Ríos",alias:"El imprevisible",foto:photos[2]}
   ],
   votes:[
     {galaId:1,concursanteId:11,espectadorId:"ejemplo1",valor:9},
     {galaId:1,concursanteId:11,espectadorId:"ejemplo2",valor:8},
     {galaId:1,concursanteId:12,espectadorId:"ejemplo1",valor:7},
     {galaId:1,concursanteId:13,espectadorId:"ejemplo2",valor:8}
   ],
   requests:[
     {id:21,usuario:"Patricia Torres",mensaje:"Me gustaría formar parte de esta nueva edición.",estado:"PENDIENTE"},
     {id:22,usuario:"Marcos Ortega",mensaje:"Estoy preparado para todas las pruebas.",estado:"PENDIENTE"}
   ],
   news:[
     {id:31,titulo:"Bienvenidos al universo de Gran Zona Marciana",resumen:"Una nueva temporada abre las puertas a nuevos participantes y grandes sorpresas.",fecha:offset(-2),
       foto:"https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&q=80"},
     {id:32,titulo:"El escenario ya está preparado",resumen:"Las primeras galas llegan con nuevas pruebas, retos y la voz del público.",fecha:offset(-1),
       foto:"https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800&q=80"},
     {id:33,titulo:"Inscripciones para la próxima edición",resumen:"Consulta las fechas y prepara tu solicitud para entrar en el programa.",fecha:offset(0),
       foto:"https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&q=80"}
   ]
 };
}
const ventanaVoto=(fecha,ahora=Date.now())=> {
 const start=new Date(fecha).getTime();
 return Number.isFinite(start)&&ahora>=start&&ahora<start+24*3600000;
};
function voto(db,rol,galaId,concursanteId,valor,espectadorId="visitante-demo"){
 const gala=db.galas.find(x=>x.id===galaId);
 if(rol!=="ESPECTADOR")return {ok:false,error:"Solo un espectador puede votar."};
 if(!gala||!ventanaVoto(gala.fecha))return {ok:false,error:"La gala no admite votos en este momento (24 horas)."};
 if(!db.contestants.some(x=>x.id===concursanteId))return {ok:false,error:"Concursante no encontrado."};
 if(!Number.isInteger(valor)||valor<1||valor>10)return {ok:false,error:"Debes puntuar entre 1 y 10."};
 if(db.votes.some(x=>x.galaId===galaId&&x.concursanteId===concursanteId&&x.espectadorId===espectadorId))
   return {ok:false,error:"Ya has puntuado a este concursante en esta gala."};
 db.votes.push({galaId,concursanteId,valor,espectadorId});
 return {ok:true};
}
function solicitud(db,rol,usuario,mensaje){
 if(rol!=="ESPECTADOR")return {ok:false,error:"Solo los espectadores registrados pueden solicitar una plaza."};
 const u=String(usuario||"").trim(),m=String(mensaje||"").trim();
 if(u.length<3||u.length>70||m.length<10||m.length>400)
   return {ok:false,error:"Nombre de 3-70 caracteres y motivación de 10-400."};
 if(new Date(db.edition.fin).getTime()<Date.now())return {ok:false,error:"La edición ya está cerrada."};
 if(db.requests.some(x=>x.usuario.toLowerCase()===u.toLowerCase()))
   return {ok:false,error:"Ya hay una solicitud de ese usuario para esta edición."};
 db.requests.push({id:Math.max(20,...db.requests.map(x=>x.id))+1,usuario:u,mensaje:m,estado:"PENDIENTE"});
 return {ok:true};
}
function decidir(db,rol,id,nuevo){
 if(rol!=="ADMINISTRADOR")return {ok:false,error:"Solo producción puede resolver solicitudes."};
 const req=db.requests.find(x=>x.id===id);
 if(!req||req.estado!=="PENDIENTE")return {ok:false,error:"Solicitud inexistente o ya resuelta."};
 if(nuevo!=="ACEPTADA"&&nuevo!=="RECHAZADA")return {ok:false,error:"Estado no permitido."};
 if(nuevo==="ACEPTADA"){
   if(db.contestants.length>=db.edition.max)
     return {ok:false,error:"Se ha alcanzado el cupo máximo de concursantes."};
   req.estado="ACEPTADA";
   db.contestants.push({id:Math.max(10,...db.contestants.map(x=>x.id))+1,nombre:req.usuario,alias:"Nuevo fichaje",foto:photos[3]});
   if(db.contestants.length>=db.edition.max){
     db.requests.forEach(x=>{if(x.estado==="PENDIENTE")x.estado="RECHAZADA"});
   }
 }else req.estado="RECHAZADA";
 return {ok:true};
}
const media=(db,id)=>{
 const votos=db.votes.filter(x=>x.concursanteId===id);
 return votos.length?votos.reduce((sum,x)=>sum+x.valor,0)/votos.length:0;
};
const rules=Object.freeze({seed,ventanaVoto,voto,solicitud,decidir,media});
if(typeof window!=="undefined")window.GranZonaMarcianaDemo=rules;
if(typeof document==="undefined")return;
const $=id=>document.getElementById(id);
const esc=x=>String(x??"").replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
const fmt=s=>new Intl.DateTimeFormat("es-ES",{day:"2-digit",month:"long",year:"numeric"}).format(new Date(s));
const day=s=>new Intl.DateTimeFormat("es-ES",{day:"2-digit"}).format(new Date(s));
const month=s=>new Intl.DateTimeFormat("es-ES",{month:"short"}).format(new Date(s)).replace(".","");
const shortDate=s=>new Intl.DateTimeFormat("es-ES",{day:"2-digit",month:"short",hour:"2-digit",minute:"2-digit"}).format(new Date(s));
const role=$("role");
let db,rol="ESPECTADOR",page="inicio",timeout;
try{
 db=JSON.parse(localStorage.getItem(KEY));
 if(!db||!Array.isArray(db.galas)||!Array.isArray(db.contestants)||!Array.isArray(db.requests)||!Array.isArray(db.votes))db=seed();
}catch(e){db=seed();}
const store=()=>{try{localStorage.setItem(KEY,JSON.stringify(db))}catch(e){}};
const notify=msg=>{const t=$("toast");t.textContent=msg;t.classList.add("visible");clearTimeout(timeout);timeout=setTimeout(()=>t.classList.remove("visible"),3300)};
const apply=(result,message)=>{if(!result.ok){notify(result.error);return false;}store();render();notify(message);return true;};
const icon='<svg class="icon"><use href="#i-arrow"/></svg>';
function showPage(p){
 page=["inicio","galas","concursantes","votaciones","solicitudes","noticias","proyecto"].includes(p)?p:"inicio";
 document.querySelectorAll(".view").forEach(x=>x.hidden=x.id!=="view-"+page);
 document.querySelectorAll("[data-page]").forEach(x=>x.classList.toggle("active",x.dataset.page===page));
 $("breadcrumb-title").textContent={
   inicio:"Panel principal",galas:"Galas y ediciones",concursantes:"Concursantes",votaciones:"Votaciones",
   solicitudes:"Solicitudes",noticias:"Actualidad",proyecto:"El proyecto"
 }[page];
 $("sidebar").classList.remove("open");$("mobile-nav").setAttribute("aria-expanded","false");
 window.scrollTo({top:0,behavior:"instant"});render();
}
function galaCard(g,i){
 return '<button type="button" class="gala-card" data-action="gala" data-id="'+g.id+'">'+
   '<span class="gala-art"><i>0'+g.id+'</i></span>'+
   '<div class="gala-info"><small>'+ (ventanaVoto(g.fecha)?"VOTACIÓN ABIERTA":new Date(g.fecha)>Date.now()?"PRÓXIMAMENTE":"CELEBRADA")+
   '</small><h3>'+esc(g.nombre)+'</h3><p>'+esc(shortDate(g.fecha))+'</p>'+
   '<span>Explorar gala →</span></div></button>';
}
function rank(){
 return [...db.contestants].sort((a,b)=>media(db,b.id)-media(db,a.id));
}
function renderHome(){
 const stats=[
   ["Concursantes",db.contestants.length,"En la edición actual"],
   ["Galas",db.galas.length,"Eventos programados"],
   ["Votos registrados",db.votes.length,"Puntuaciones de ejemplo"],
   ["Solicitudes",db.requests.filter(x=>x.estado==="PENDIENTE").length,"Por revisar"]
 ];
 $("stats").innerHTML=stats.map(x=>'<article class="stat"><span>'+x[0]+'</span><strong>'+x[1]+'</strong><small>'+x[2]+'</small></article>').join("");
 $("home-galas").innerHTML=db.galas.slice(0,2).map(galaCard).join("");
 $("home-ranking").innerHTML='<div class="ranking-wrap">'+rank().slice(0,3).map((c,i)=>
   '<div class="ranking-row"><span class="rank-num">0'+(i+1)+'</span><span class="rank-avatar">'+esc(c.nombre.slice(0,1))+
   '</span><div><strong>'+esc(c.nombre)+'</strong><small>'+esc(c.alias)+'</small></div>'+
   '<span>'+ (media(db,c.id)?media(db,c.id).toFixed(1):"—") +'/10</span></div>').join("")+'</div>';
 $("home-news").innerHTML='<div class="news-preview-wrap">'+[...db.news].reverse().slice(0,3).map(n=>
   '<button class="news-preview" data-action="news" data-id="'+n.id+'" type="button" style="width:100%;border-left:0;border-right:0;border-bottom:0;background:none;text-align:left;">'+
   '<img src="'+esc(n.foto)+'" alt="" loading="lazy"><div><strong>'+esc(n.titulo)+'</strong><small>'+fmt(n.fecha)+'</small></div></button>').join("")+'</div>';
}
function renderGalas(){
 $("edition-strip").innerHTML='<div><span>EDICIÓN ACTIVA</span><br><strong>'+esc(db.edition.nombre)+'</strong></div>'+
  '<small>'+fmt(db.edition.inicio)+' — '+fmt(db.edition.fin)+'<br>Cupo: '+db.contestants.length+'/'+db.edition.max+' concursantes</small>';
 $("gala-count").textContent=db.galas.length+" galas";
 $("gala-list").innerHTML=db.galas.map(g=>
  '<article class="gala-row"><div class="gala-row-left"><div class="gala-date"><strong>'+day(g.fecha)+
  '</strong><span>'+month(g.fecha).toUpperCase()+'</span></div><div><h3>'+esc(g.nombre)+
  '</h3><p>'+esc(shortDate(g.fecha))+' · '+(ventanaVoto(g.fecha)?"Votación abierta":new Date(g.fecha)>Date.now()?"Próxima gala":"Finalizada")+
  '</p></div></div><button class="outline-button" type="button" data-action="gala" data-id="'+g.id+'">Detalles '+icon+'</button></article>').join("");
}
function renderContestants(){
 $("contestants").innerHTML=rank().map((c,i)=>
  '<button class="person-card" type="button" data-action="person" data-id="'+c.id+'">'+
  '<div class="person-portrait"><img src="'+esc(c.foto)+'" alt="" loading="lazy"></div>'+
  '<div class="person-footer"><small>CONCURSANTE / '+String(i+1).padStart(2,"0")+
  '</small><strong>'+esc(c.nombre)+'</strong><span>'+esc(c.alias)+' · Media '+(media(db,c.id)?media(db,c.id).toFixed(1):"—")+
  '/10</span></div></button>').join("");
}
function renderVotes(){
 const gala=db.galas.find(g=>ventanaVoto(g.fecha));
 $("voting-info").textContent=rol==="ESPECTADOR"?
   (gala?"Votación de "+gala.nombre+" abierta. En esta simulación solo puedes votar una vez por concursante y gala.":
    "No hay galas dentro de su ventana de 24 horas."):
   "Para votar, selecciona el perfil Espectador. El selector solo simula una sesión, no autentica usuarios reales.";
 $("vote-list").innerHTML=db.contestants.map(c=>{
   const ya=db.votes.some(v=>v.espectadorId==="visitante-demo"&&v.galaId===gala?.id&&v.concursanteId===c.id);
   return '<article class="vote-row"><img class="vote-photo" src="'+esc(c.foto)+'" alt="" loading="lazy">'+
   '<div><h3>'+esc(c.nombre)+'</h3><p>'+esc(c.alias)+' · Media '+(media(db,c.id)?media(db,c.id).toFixed(1):"—")+'/10</p></div>'+
   '<button type="button" class="outline-button" data-action="vote" data-id="'+c.id+'" '+(!gala||ya||rol!=="ESPECTADOR"?"disabled":"")+'>'+
   (ya?"Ya has votado":"Puntuar")+'</button></article>';
 }).join("");
}
const status=s=>'<span class="status '+esc(s)+'">'+(s==="PENDIENTE"?"Pendiente":s==="ACEPTADA"?"Aceptada":"Rechazada")+'</span>';
function renderRequests(){
 $("request-top").innerHTML='<h3>'+ (rol==="ADMINISTRADOR"?"Gestión de aspirantes":"¿Quieres entrar en el programa?")+'</h3>'+
  '<p>'+ (rol==="ADMINISTRADOR"?"Aprueba solicitudes sin superar el límite de "+db.edition.max+" concursantes.": "Envía una motivación para solicitar tu participación en la edición de ejemplo.")+'</p>'+
  (rol==="ESPECTADOR"?'<button class="hero-button" type="button" data-action="request">Enviar solicitud '+icon+'</button>':'');
 $("request-count").textContent=db.requests.length+" solicitudes";
 $("requests").innerHTML=db.requests.length?db.requests.map(s=>
  '<article class="request-row"><div><h3>'+esc(s.usuario)+'</h3><p>'+esc(s.mensaje)+'</p></div>'+
  '<div class="request-actions">'+status(s.estado)+(rol==="ADMINISTRADOR"&&s.estado==="PENDIENTE"?
   '<button class="mini-button approve" type="button" data-action="approve" data-id="'+s.id+'">Aceptar</button>'+
   '<button class="mini-button reject" type="button" data-action="reject" data-id="'+s.id+'">Rechazar</button>':"")+
   '</div></article>').join(""):'<div class="empty">No hay solicitudes todavía.</div>';
}
function renderNews(){
 $("news-grid").innerHTML=[...db.news].reverse().map(n=>
   '<button type="button" class="news-card" data-action="news" data-id="'+n.id+'">'+
   '<img src="'+esc(n.foto)+'" alt="" loading="lazy"><div class="news-card-body"><small>'+fmt(n.fecha)+
   '</small><h3>'+esc(n.titulo)+'</h3><p>'+esc(n.resumen)+'</p></div></button>').join("");
}
function render(){renderHome();renderGalas();renderContestants();renderVotes();renderRequests();renderNews();}
const dialog=$("details"),content=$("modal-content");
const open=html=>{content.innerHTML=html;dialog.showModal()};
const close=()=>dialog.close();
const heading=(eyebrow,title,subtitle)=>'<div class="modal-header"><small>'+esc(eyebrow)+'</small>'+
 '<h2 id="modal-title">'+esc(title)+'</h2><p>'+esc(subtitle)+'</p></div>';
function personDetails(id){
 const c=db.contestants.find(x=>x.id===id);if(!c)return;
 const votes=db.votes.filter(x=>x.concursanteId===id);
 open(heading("EXPEDIENTE · CONCURSANTE",c.nombre,c.alias)+
  '<div class="modal-body"><div class="modal-data">'+
  '<div><strong>'+votes.length+'</strong><span>Votos recibidos</span></div>'+
  '<div><strong>'+(media(db,id)?media(db,id).toFixed(1):"—")+'</strong><span>Media sobre 10</span></div>'+
  '<div><strong>'+db.galas.length+'</strong><span>Galas en programa</span></div></div>'+
  '<p>Este perfil es ficticio y muestra cómo la aplicación Android presenta sus datos relacionados con las galas y las puntuaciones del público.</p></div>');
}
function galaDetails(id){
 const gala=db.galas.find(x=>x.id===id);if(!gala)return;
 const active=ventanaVoto(gala.fecha);
 open(heading("TEMPORADA 01 / GALA "+String(id).padStart(2,"0"),gala.nombre,shortDate(gala.fecha))+
  '<div class="modal-body"><p>'+esc(gala.resumen)+'</p>'+
  '<div class="modal-data"><div><strong>'+db.contestants.length+'</strong><span>Participantes</span></div>'+
  '<div><strong>'+db.votes.filter(v=>v.galaId===id).length+'</strong><span>Votos registrados</span></div>'+
  '<div><strong>'+(active?"Abierta":"Cerrada")+'</strong><span>Votación</span></div></div>'+
  (active?'<button type="button" class="hero-button" data-action="go-votes">Ir a las votaciones '+icon+'</button>':'')+'</div>');
}
function newsDetails(id){
 const n=db.news.find(x=>x.id===id);if(!n)return;
 open(heading("ÚLTIMAS NOTICIAS / "+fmt(n.fecha),n.titulo,"Actualidad de Gran Zona Marciana")+
 '<div class="modal-body"><img src="'+esc(n.foto)+'" alt="" style="width:100%;max-height:290px;object-fit:cover;border-radius:8px;margin-bottom:19px"><p>'+esc(n.resumen)+'</p></div>');
}
function voteDialog(id){
 const c=db.contestants.find(x=>x.id===id),g=db.galas.find(x=>ventanaVoto(x.fecha));if(!c||!g)return;
 open(heading("VOTACIONES / GALA "+g.id,"Puntuar a "+c.nombre,"Un voto por concursante y gala. Puntuación de 1 a 10.")+
 '<div class="modal-body"><form id="form-vote"><input type="hidden" name="contestant" value="'+id+'">'+
 '<input type="hidden" name="gala" value="'+g.id+'"><div class="field"><label for="score">Tu puntuación</label>'+
 '<select name="score" id="score">'+Array.from({length:10},(_,i)=>'<option value="'+(i+1)+'" '+(i===7?"selected":"")+'> '+(i+1)+' / 10</option>').join("")+
 '</select></div><button type="submit" class="form-submit">Confirmar puntuación</button></form></div>');
}
function requestDialog(){
 open(heading("INSCRIPCIONES / TEMPORADA 01","Presenta tu solicitud","Indica tu nombre y motivación para solicitar una plaza.")+
 '<div class="modal-body"><form id="form-request"><div class="field"><label for="applicant-name">Nombre</label>'+
 '<input id="applicant-name" name="name" required minlength="3" maxlength="70" placeholder="Ej. Elena Martín"></div>'+
 '<div class="field"><label for="applicant-text">¿Por qué quieres participar?</label>'+
 '<textarea id="applicant-text" name="message" required minlength="10" maxlength="400" placeholder="Cuéntanos tu motivación..."></textarea></div>'+
 '<span class="form-note">Solo para la simulación del navegador. No se envían datos a ningún servidor.</span>'+
 '<button type="submit" class="form-submit">Enviar solicitud</button></form></div>');
}
document.addEventListener("click",e=>{
 const to=e.target.closest("[data-goto]");
 if(to){location.hash=to.dataset.goto;return;}
 const btn=e.target.closest("[data-action]");if(!btn)return;
 const action=btn.dataset.action,id=Number(btn.dataset.id);
 if(action==="gala")galaDetails(id);
 if(action==="person")personDetails(id);
 if(action==="news")newsDetails(id);
 if(action==="vote")voteDialog(id);
 if(action==="request")requestDialog();
 if(action==="approve")apply(decidir(db,rol,id,"ACEPTADA"),"Solicitud aprobada en esta simulación.");
 if(action==="reject")apply(decidir(db,rol,id,"RECHAZADA"),"Solicitud rechazada en esta simulación.");
 if(action==="go-votes"){close();location.hash="votaciones";}
});
document.addEventListener("submit",e=>{
 const form=e.target;if(!["form-vote","form-request"].includes(form.id))return;
 e.preventDefault();
 const data=new FormData(form);let result,message;
 if(form.id==="form-vote"){
   result=voto(db,rol,Number(data.get("gala")),Number(data.get("contestant")),Number(data.get("score")));
   message="Puntuación guardada en este navegador.";
 }else{
   result=solicitud(db,rol,data.get("name"),data.get("message"));
   message="Solicitud enviada en la simulación.";
 }
 if(result.ok)close();apply(result,message);
});
role.addEventListener("change",e=>{rol=e.target.value;render();notify("Vista de "+e.target.selectedOptions[0].text+" activada.");});
$("reset").addEventListener("click",()=>{db=seed();store();render();notify("Datos ficticios restaurados.");});
$("close-modal").addEventListener("click",close);
dialog.addEventListener("click",e=>{if(e.target===dialog)close();});
$("mobile-nav").addEventListener("click",()=>{$("sidebar").classList.toggle("open");$("mobile-nav").setAttribute("aria-expanded",String($("sidebar").classList.contains("open")));});
window.addEventListener("hashchange",()=>showPage(location.hash.slice(1)));
showPage(location.hash.slice(1));
})();