import { clinica } from '../../api/servicios'
import { ListaEntradas, ResumenCifras } from '../../componentes/EntradasClinicas'
import { ErrorAviso, Esqueleto, Pagina, Vacio } from '../../componentes/ui'
import { useAsync } from '../../util/useAsync'

export function MiHistoria() {
  const historia = useAsync(() => clinica.miHistoria(), [])
  const resumen = useAsync(() => clinica.miResumen(), [])
  return (
    <Pagina titulo="Mi historia clinica" descripcion="Solo tu y los profesionales que te atendieron pueden ver estos datos.">
      {resumen.datos && <ResumenCifras r={resumen.datos} />}
      {historia.cargando ? <Esqueleto /> : historia.error != null ? <ErrorAviso error={historia.error} alReintentar={historia.recargar} /> : !historia.datos?.length ? (
        <Vacio titulo="Todavia no tienes atenciones registradas">Despues de cada consulta, el profesional registra aqui el motivo, el diagnostico y las indicaciones.</Vacio>
      ) : (
        <div className="panel"><ListaEntradas entradas={historia.datos} /></div>
      )}
    </Pagina>
  )
}
