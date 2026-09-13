const s=(r,n)=>{if(r.length<=n)return r;const o=r.slice(0,Math.floor(n/2)-1),t=r.slice(-Math.floor(n/2)+1);return`${o}...${t}`};export{s as t};
