import createCrudRequest from '@/api/crud'

export interface Workflow {
    id?: string;
    name: string;
    flowStr: string;
    desc?: string;
    tags?: string[];
    plugins?: string[];
    creator?: string;
}

const workflowApi = createCrudRequest<Workflow, string>('/workflows')
export default workflowApi; 
