type ClauseType = "AND" | "OR"
const Clause: Record<string, ClauseType> = {
    AND: "AND",
    OR: "OR"
}

interface Condition {
    dataKey?: string;
    calcType?: string;
    value?: any;
    clause: ClauseType;
    children?: Condition[];
}

export { Clause, type Condition, type ClauseType };