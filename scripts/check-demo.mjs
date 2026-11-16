import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { runInNewContext } from "node:vm";

const html=readFileSync(new URL("../docs/index.html",import.meta.url),"utf8");
const css=readFileSync(new URL("../docs/styles.css",import.meta.url),"utf8");
const script=readFileSync(new URL("../docs/app.js",import.meta.url),"utf8");

for(const id of ["stats","home-galas","home-ranking","contestants","voting-info","requests","news-grid","details","role"]){
 assert.ok(html.includes('id="'+id+'"'),"Falta sección: "+id);
}
assert.match(css,/\.shell\{display:grid/);
assert.match(css,/@media\(max-width:600px\)/);
assert.match(html,/Vista interactiva/);
assert.match(html,/datos ficticios/i);

const window={};
runInNewContext(script,{window,console});
const api=window.GranZonaMarcianaDemo;
assert.ok(api);
const db=api.seed();
assert.equal(db.contestants.length,3);
assert.equal(db.galas.length,3);
const live=db.galas.find(x=>api.ventanaVoto(x.fecha));
assert.ok(live,"La demo debe incluir una gala en periodo de voto");
assert.equal(api.ventanaVoto(new Date(Date.now()-25*3600000).toISOString()),false);
assert.equal(api.ventanaVoto(new Date(Date.now()+3600000).toISOString()),false);
assert.equal(api.voto(db,"INVITADO",live.id,11,9).ok,false);
assert.equal(api.voto(db,"ESPECTADOR",live.id,11,11).ok,false);
assert.equal(api.voto(db,"ESPECTADOR",live.id,11,9).ok,true);
assert.equal(api.voto(db,"ESPECTADOR",live.id,11,10).ok,false,"No duplicar votos");
assert.equal(api.voto(db,"ESPECTADOR",live.id,999,10).ok,false,"Concursante inexistente");
assert.equal(api.solicitud(db,"CONCURSANTE","Sara Luna","Quiero participar en el reality").ok,false);
assert.equal(api.solicitud(db,"ESPECTADOR","Sara Luna","Quiero participar en el reality").ok,true);
assert.equal(api.solicitud(db,"ESPECTADOR","Sara Luna","Otra descripción bastante larga").ok,false);
const id=db.requests.find(x=>x.usuario==="Sara Luna").id;
assert.equal(api.decidir(db,"ESPECTADOR",id,"ACEPTADA").ok,false);
assert.equal(api.decidir(db,"ADMINISTRADOR",id,"ACEPTADA").ok,true);
assert.equal(db.contestants.some(x=>x.nombre==="Sara Luna"),true);
assert.equal(api.decidir(db,"ADMINISTRADOR",id,"ACEPTADA").ok,false);
assert.equal(api.decidir(db,"ADMINISTRADOR",21,"ACEPTADA").ok,true);
assert.equal(db.contestants.length,db.edition.max);
assert.equal(db.requests.find(x=>x.id===22).estado,"RECHAZADA","Al llenarse el cupo se cierran pendientes");
console.log("OK · Gran Zona Marciana: navegación, gala, votación, inscripciones y cupo.");
