import type { ValidConnectionFunc } from '@vue-flow/core'
import type { Connection } from '@vue-flow/core'
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
const validConnection: ValidConnectionFunc = (connection: Connection): boolean => {
    return getHandleDirection(connection.sourceHandle) !== getHandleDirection(connection.targetHandle)
}

export { getHandleDirection, validConnection }