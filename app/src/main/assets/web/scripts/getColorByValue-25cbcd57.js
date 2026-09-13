const r=(f,o=100,i=!1)=>{f=f===0?0:f/o*100,i&&(f=o-f);let c="#65a30d";return f>25&&(c="#16a34a"),f>50&&(c="#facc15"),f>75&&(c="#ea580c"),f>90&&(c="#dc2626"),c};export{r as g};
