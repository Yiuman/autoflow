type ClauseType = "AND" | "OR"
const Clause: Record<string, ClauseType> = {
    AND: "AND",
    OR: "OR"
}
enum CalcType {
    Express = 'Express',
    Equal = 'Equal',
    NotEqual = 'NotEqual',
    LT = 'LT',
    GT = 'GT',
    LTE = 'LTE',
    GTE = 'GTE',
    Like = 'Like',
    In = 'In',
    Empty = 'Empty',
    NotEmpty = 'NotEmpty'
}

interface Condition {
    root?: boolean
    dataKey?: string;
    calcType?: CalcType;
    value?: any;
    clause: ClauseType;
    children?: Condition[];
}

export { Clause, type Condition, type ClauseType, CalcType };