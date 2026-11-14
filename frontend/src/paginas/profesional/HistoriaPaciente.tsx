import { ArrowLeft } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'
import { clinica } from '../../api/servicios'
import { ListaEntradas, ResumenCifras } from '../../componentes/EntradasClinicas'
import { Aviso, ErrorAviso, Esqueleto, Pagina, Vacio } from '../../componentes/ui'
import { ApiError } from '../../api/http'
import { useNombresPacientes } from '../../util/useNombresPacientes'
import { useAsync } from '../../util/useAsync'

export function HistoriaPaciente() {
  const { pacienteId = '' } = useParams()
  const historia = useAsync(() => clinica.historiaDe(pacienteId), [pacienteId])
  const resumen = useAsync(() => clinica.resumenDe(pacienteId), [pacienteId])
  const paciente = useNombresPacientes([pacienteId])
  const sinAcceso = historia.error instanceof ApiError && historia.error.estado === 403

  return (
    <Pagina titulo={`Historia de ${paciente(pacienteId)}`} descripcion="Solo disponible si tienes o tuviste citas con este paciente. El acceso queda registrado."
      acciones={<Link to="/agenda" className="btn btn-secundario"><ArrowLeft size={16} aria-hidden /> Volver a la agenda</Link>}>
      {resumen.datos && <ResumenCifras r={resumen.datos} />}
      {historia.cargando ? <Esqueleto /> : sinAcceso ? (
        <Aviso tipo="aviso">No puedes ver la historia de este paciente: solo se muestra a profesionales con relacion de citas. Este intento quedo registrado.</Aviso>
      ) : historia.error != null ? <ErrorAviso error={historia.error} alReintentar={historia.recargar} /> : !historia.datos?.length ? (
        <Vacio titulo="Sin entradas todavia">Cuando registres la atencion de una cita, aparecera aqui.</Vacio>
      ) : <div className="panel"><ListaEntradas entradas={historia.datos} /></div>}
    </Pagina>
  )
}
