import { useState } from 'react'
import { Link } from 'react-router-dom'
import { clinica } from '../../api/servicios'
import type { Entrada } from '../../api/tipos'
import { AtencionForm } from '../../componentes/AtencionForm'
import { ListaEntradas } from '../../componentes/EntradasClinicas'
import { useToast } from '../../componentes/Toasts'
import { Boton, ErrorAviso, Esqueleto, Pagina, Paginador, Vacio } from '../../componentes/ui'
import { idCorto } from '../../util/formato'
import { useAsync } from '../../util/useAsync'

export function Atenciones() {
  const [pagina, setPagina] = useState(0)
  const lista = useAsync(() => clinica.atenciones({ page: pagina, size: 10 }), [pagina])
  const [corrigiendo, setCorrigiendo] = useState<string | null>(null)
  const avisar = useToast()

  return (
    <Pagina titulo="Mis atenciones" descripcion="Las entradas que registraste, de la mas reciente a la mas antigua. Cada lectura queda en el registro de acceso.">
      {lista.cargando ? <Esqueleto /> : lista.error != null ? <ErrorAviso error={lista.error} alReintentar={lista.recargar} /> : !lista.datos?.contenido.length ? (
        <Vacio titulo="Aun no has registrado atenciones">Desde la agenda puedes registrar la atencion de una cita confirmada.</Vacio>
      ) : (
        <>
          <div className="panel">
            <ListaEntradas entradas={[...lista.datos.contenido].reverse()} accion={(e: Entrada) => (
              <>
                <Link to={`/pacientes/${e.pacienteId}`} className="item-sub">Paciente {idCorto(e.pacienteId)}</Link>
                <Boton variante="fantasma" className="btn-chico" onClick={() => setCorrigiendo(corrigiendo === e.id ? null : e.id)}>Corregir</Boton>
                {corrigiendo === e.id && (
                  <div style={{ flexBasis: '100%' }}>
                    <AtencionForm entradaACorregir={e} alCancelar={() => setCorrigiendo(null)} alGuardar={() => { avisar('Correccion registrada.'); setCorrigiendo(null); lista.recargar() }} />
                  </div>
                )}
              </>
            )} />
          </div>
          <Paginador pagina={lista.datos.pagina} totalPaginas={lista.datos.totalPaginas} total={lista.datos.totalElementos} alCambiar={setPagina} />
        </>
      )}
    </Pagina>
  )
}
