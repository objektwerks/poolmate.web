package poolmate

import com.raquo.laminar.api.L.*

import org.scalajs.dom.console.log

object Model:
  val emailAddressVar = Var("")
  val pinVar = Var("")
  val account = Var(Account.empty)
  val pools = Model[Pool](Var(Seq.empty[Pool]), Var(Pool()), Pool())

final case class Model[E <: Entity](entitiesVar: Var[Seq[E]],
                                    selectedEntityVar: Var[E],
                                    emptyEntity: E):
  given owner: Owner = new Owner {}
  entitiesVar.signal.foreach(entities => log(s"entities model -> ${entities.toString}"))
  selectedEntityVar.signal.foreach(entity => log(s"selected entity -> ${entity.toString}"))

  def setSelectedEntityById(id: Long): Model[E] =
    selectedEntityVar.set(entitiesVar.now().find(_.id == id).getOrElse(emptyEntity))
    this

  def updateSelectedEntity(updatedSelectedEntity: E): Unit =
    entitiesVar.update { entities =>
      entities.map { entity =>
        if entity.id == updatedSelectedEntity.id then
          selectedEntityVar.set(updatedSelectedEntity)
          updatedSelectedEntity
        else entity
      }
    }