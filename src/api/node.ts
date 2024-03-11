import type { Node } from '@/types/flow';
import request from '@/utils/request';
enum API{
    NODE_LIST="/nodes"
}

export function getNodeList(){
    return request.get<Node[]>(API.NODE_LIST);
}
