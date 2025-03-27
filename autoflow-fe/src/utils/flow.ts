import type {Connection} from '@vue-flow/core'
import type {ExecutionResult} from '@/types/flow'

/**
 * 获取连接的处理器的类型（input\output）
 * @param handle 连接处理器的ID
 */
function getHandleDirection(handle: string | null | undefined): string {
  return handle?.substring(handle?.lastIndexOf('-') + 1) ?? ''
}

/**
 * 校验连接
 */
function validateConnection(connection: Connection): boolean {
  if (connection.source === connection.target) {
    return false
  }

  return getHandleDirection(connection.sourceHandle) !== getHandleDirection(connection.targetHandle)
}

function getResultData<T>(result: ExecutionResult<T> | ExecutionResult<T>[]): undefined | T | T[] {
  if (!result) {
    return undefined
  }
  if (result instanceof Array) {
    return result.map((r) => {
      return {
        ...r.data,
        error: r.error
      }
    }) as T[]
  }
  return result.data as T
}

function getResultFirstData<T>(result: ExecutionResult<T> | ExecutionResult<T>[]): undefined | T {
  if (!result) {
    return undefined
  }
  if (result instanceof Array) {
    return result.filter((r) => r).map((r) => r.data)[0] as T
  }
  return result.data as T
}

function getResultFirst<T>(
  result: ExecutionResult<T> | ExecutionResult<T>[]
): undefined | ExecutionResult<T> {
  if (!result) {
    return undefined
  }

  if (result instanceof Array) {
    return result[0] as ExecutionResult<T>
  }
  return result
}

function getExecutionDurationSeconds<T>(result: ExecutionResult<T> | ExecutionResult<T>[]) {
  if (!result) {
    return undefined
  }

  if (result instanceof Array) {
    const notErrorResult = result
      .filter((r) => !r.error);
    if(!notErrorResult || !notErrorResult.length) {
      return undefined;
    }
    const maxEndTime =
      notErrorResult.reduce((max, obj) => {
        return max.endTime > obj.endTime ? max : obj
      })
    const minStartTime =notErrorResult
      .reduce((min, obj) => {
        return min.startTime < obj.startTime ? min : obj
      })
    return ((maxEndTime.endTime - minStartTime.startTime) / 1000).toFixed(3)
    // const totalDuration = result.reduce((acc, r) => acc + (r?.durationMs || 0), 0)
    // return (totalDuration / 1000).toFixed(3)
  }
  return ((result?.durationMs || 0) / 1000).toFixed(3)
}

export {
  getResultData,
  getResultFirstData,
  getResultFirst,
  getHandleDirection,
  validateConnection,
  getExecutionDurationSeconds
}
