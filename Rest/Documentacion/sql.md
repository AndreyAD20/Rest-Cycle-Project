
-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.apps_vinculadas (
  id integer NOT NULL DEFAULT nextval('apps_vinculadas_id_seq'::regclass),
  iddispositivo integer,
  nombre character varying NOT NULL,
  categoria character varying,
  tiempolimite integer,
  tiempouso integer,
  CONSTRAINT apps_vinculadas_pkey PRIMARY KEY (id),
  CONSTRAINT apps_vinculadas_iddispositivo_fkey FOREIGN KEY (iddispositivo) REFERENCES public.dispositivos(id)
);
CREATE TABLE public.codigos_recuperacion (
  id integer NOT NULL DEFAULT nextval('codigos_recuperacion_id_seq'::regclass),
  correo character varying NOT NULL,
  codigo character varying NOT NULL,
  fecha_creacion timestamp without time zone DEFAULT now(),
  usado boolean DEFAULT false,
  CONSTRAINT codigos_recuperacion_pkey PRIMARY KEY (id)
);
CREATE TABLE public.conexion_parentales (
  idpadre integer NOT NULL,
  idhijo integer NOT NULL,
  contrasenasegura character varying NOT NULL,
  CONSTRAINT conexion_parentales_pkey PRIMARY KEY (idpadre, idhijo),
  CONSTRAINT conexion_parentales_idpadre_fkey FOREIGN KEY (idpadre) REFERENCES public.usuario(id),
  CONSTRAINT conexion_parentales_idhijo_fkey FOREIGN KEY (idhijo) REFERENCES public.usuario(id)
);
CREATE TABLE public.dias (
  id integer NOT NULL DEFAULT nextval('dias_id_seq'::regclass),
  nombre character varying NOT NULL,
  CONSTRAINT dias_pkey PRIMARY KEY (id)
);
CREATE TABLE public.dias_horarios (
  idhorario integer NOT NULL,
  iddia integer NOT NULL,
  CONSTRAINT dias_horarios_pkey PRIMARY KEY (idhorario, iddia),
  CONSTRAINT dias_horarios_idhorario_fkey FOREIGN KEY (idhorario) REFERENCES public.horarios(id),
  CONSTRAINT dias_horarios_iddia_fkey FOREIGN KEY (iddia) REFERENCES public.dias(id)
);
CREATE TABLE public.dispositivos (
  id integer NOT NULL DEFAULT nextval('dispositivos_id_seq'::regclass),
  idusuario integer,
  nombre character varying,
  ip character varying NOT NULL,
  estado character varying,
  CONSTRAINT dispositivos_pkey PRIMARY KEY (id),
  CONSTRAINT dispositivos_idusuario_fkey FOREIGN KEY (idusuario) REFERENCES public.usuario(id)
);
CREATE TABLE public.historial_ubicacion (
  id integer NOT NULL DEFAULT nextval('historial_ubicacion_id_seq'::regclass),
  id_ubicacion character varying NOT NULL,
  fecha date NOT NULL,
  hora time without time zone NOT NULL,
  id_usuario integer,
  CONSTRAINT historial_ubicacion_pkey PRIMARY KEY (id),
  CONSTRAINT historial_ubicacion_id_ubicacion_fkey FOREIGN KEY (id_ubicacion) REFERENCES public.ubicacion(place_id),
  CONSTRAINT historial_ubicacion_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id)
);
CREATE TABLE public.horarios (
  id integer NOT NULL DEFAULT nextval('horarios_id_seq'::regclass),
  iddispositivo integer,
  idmedida integer,
  horainicio time without time zone NOT NULL,
  horafin time without time zone NOT NULL,
  CONSTRAINT horarios_pkey PRIMARY KEY (id),
  CONSTRAINT horarios_iddispositivo_fkey FOREIGN KEY (iddispositivo) REFERENCES public.dispositivos(id),
  CONSTRAINT horarios_idmedida_fkey FOREIGN KEY (idmedida) REFERENCES public.medida(id)
);
CREATE TABLE public.medida (
  id integer NOT NULL DEFAULT nextval('medida_id_seq'::regclass),
  nombre character varying NOT NULL,
  descripcion character varying NOT NULL,
  estado character varying NOT NULL,
  CONSTRAINT medida_pkey PRIMARY KEY (id)
);
CREATE TABLE public.notas (
  id integer NOT NULL DEFAULT nextval('notas_id_seq'::regclass),
  idusuario integer,
  titulo character varying,
  contenido text,
  CONSTRAINT notas_pkey PRIMARY KEY (id),
  CONSTRAINT notas_idusuario_fkey FOREIGN KEY (idusuario) REFERENCES public.usuario(id)
);
CREATE TABLE public.notificaciones (
  id integer NOT NULL DEFAULT nextval('notificaciones_id_seq'::regclass),
  usuario_id integer,
  mensaje text NOT NULL,
  fecha timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
  leida boolean NOT NULL DEFAULT false,
  CONSTRAINT notificaciones_pkey PRIMARY KEY (id),
  CONSTRAINT notificaciones_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuario(id)
);
CREATE TABLE public.soporte_tecnico (
  id integer NOT NULL DEFAULT nextval('soporte_tecnico_id_seq'::regclass),
  idusuario integer,
  canal character varying NOT NULL,
  mensaje text NOT NULL,
  fecha date NOT NULL,
  CONSTRAINT soporte_tecnico_pkey PRIMARY KEY (id),
  CONSTRAINT soporte_tecnico_idusuario_fkey FOREIGN KEY (idusuario) REFERENCES public.usuario(id)
);
CREATE TABLE public.suscripciones (
  id integer NOT NULL DEFAULT nextval('suscripciones_id_seq'::regclass),
  idusuario integer,
  tipo character varying NOT NULL,
  fechainicio date NOT NULL,
  fechafin date NOT NULL,
  fecharenovacion date,
  estado character varying,
  CONSTRAINT suscripciones_pkey PRIMARY KEY (id),
  CONSTRAINT suscripciones_idusuario_fkey FOREIGN KEY (idusuario) REFERENCES public.usuario(id)
);
CREATE TABLE public.tarea (
  id integer NOT NULL DEFAULT nextval('tarea_id_seq'::regclass),
  idusuario integer,
  titulo character varying NOT NULL,
  descripcion character varying NOT NULL,
  fechainicio date NOT NULL,
  fechafin date,
  completada boolean NOT NULL,
  tipo character varying NOT NULL,
  CONSTRAINT tarea_pkey PRIMARY KEY (id),
  CONSTRAINT tarea_idusuario_fkey FOREIGN KEY (idusuario) REFERENCES public.usuario(id)
);
CREATE TABLE public.ubicacion (
  latitud numeric NOT NULL,
  longitud numeric NOT NULL,
  direccion character varying,
  place_id character varying NOT NULL,
  nombre character varying,
  CONSTRAINT ubicacion_pkey PRIMARY KEY (place_id)
);
CREATE TABLE public.url (
  id integer NOT NULL DEFAULT nextval('url_id_seq'::regclass),
  nombre character varying,
  url character varying NOT NULL UNIQUE,
  CONSTRAINT url_pkey PRIMARY KEY (id)
);
CREATE TABLE public.url_restringidas (
  id integer NOT NULL DEFAULT nextval('url_restringidas_id_seq'::regclass),
  iddispositivo integer,
  idurl integer,
  fechabloqueo date NOT NULL,
  CONSTRAINT url_restringidas_pkey PRIMARY KEY (id),
  CONSTRAINT url_restringidas_iddispositivo_fkey FOREIGN KEY (iddispositivo) REFERENCES public.dispositivos(id),
  CONSTRAINT url_restringidas_idurl_fkey FOREIGN KEY (idurl) REFERENCES public.url(id)
);
CREATE TABLE public.url_visitadas (
  id integer NOT NULL DEFAULT nextval('url_visitadas_id_seq'::regclass),
  idurl integer,
  iddispositivo integer,
  fechaacceso timestamp without time zone NOT NULL,
  CONSTRAINT url_visitadas_pkey PRIMARY KEY (id),
  CONSTRAINT url_visitadas_idurl_fkey FOREIGN KEY (idurl) REFERENCES public.url(id),
  CONSTRAINT url_visitadas_iddispositivo_fkey FOREIGN KEY (iddispositivo) REFERENCES public.dispositivos(id)
);
CREATE TABLE public.usuario (
  id integer NOT NULL DEFAULT nextval('usuario_id_seq'::regclass),
  nombre character varying NOT NULL,
  apellido character varying,
  correo character varying NOT NULL UNIQUE,
  telefono character varying NOT NULL,
  fechanacimiento date NOT NULL,
  contraseña character varying NOT NULL,
  rol character varying NOT NULL,
  Mayoredad boolean NOT NULL,
  CONSTRAINT usuario_pkey PRIMARY KEY (id)
);
